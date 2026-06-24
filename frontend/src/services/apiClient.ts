import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { store } from '@/store';
import { logout, refreshTokenSuccess } from '@/store/slices/authSlice';
import { showNotification } from '@/store/slices/uiSlice';

// Create API Client with configured default environment URL
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000, // 15 seconds request timeout
});

// Flag to track token refresh state
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value: unknown) => void;
  reject: (error: unknown) => void;
}> = [];

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// ==============================================================================
// Request Interceptor: Inject Bearer Token
// ==============================================================================
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const state = store.getState();
    const token = state.auth.token;

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// ==============================================================================
// Response Interceptor: Unified Error and Refresh Actions
// ==============================================================================
apiClient.interceptors.response.use(
  (response) => {
    // Return wrapped api wrapper response data block if exists
    return response.data;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config;
    if (!originalRequest) {
      return Promise.reject(error);
    }

    const status = error.response?.status;

    // Standardized API error payload format mapping
    const apiError = (error.response?.data as { message?: string }) || {};
    const errorMessage = apiError.message || error.message || 'An unexpected connection error occurred';

    // Handle 401 Unauthorized - Attempt Token Refresh
    // Avoid infinite loop if refreshing fails with 401
    const isRefreshEndpoint = originalRequest.url?.includes('/auth/refresh');
    if (status === 401 && !isRefreshEndpoint) {
      // Retyped original request config extension to prevent loops
      const customConfig = originalRequest as InternalAxiosRequestConfig & { _retry?: boolean };

      if (customConfig._retry) {
        // If we already retried and failed, log out user
        store.dispatch(logout());
        store.dispatch(
          showNotification({
            message: 'Your session has expired. Please sign in again.',
            severity: 'warning',
          })
        );
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // Queue this request and wait for the new token
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (customConfig.headers) {
              customConfig.headers.Authorization = `Bearer ${token}`;
            }
            return apiClient(customConfig);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      customConfig._retry = true;
      isRefreshing = true;

      const state = store.getState();
      const refreshToken = state.auth.refreshToken;

      if (!refreshToken) {
        store.dispatch(logout());
        return Promise.reject(error);
      }

      try {
        // Hit token refresh route (without interceptor token to prevent recursive loops)
        const refreshResponse = await axios.post<{
          success: boolean;
          data: { token: string; refreshToken: string };
        }>(
          `${apiClient.defaults.baseURL}/auth/refresh`,
          { refreshToken },
          { headers: { 'Content-Type': 'application/json' } }
        );

        const { token: newAccessToken, refreshToken: newRefreshToken } = refreshResponse.data.data;

        store.dispatch(
          refreshTokenSuccess({
            token: newAccessToken,
            refreshToken: newRefreshToken,
          })
        );

        processQueue(null, newAccessToken);
        isRefreshing = false;

        if (customConfig.headers) {
          customConfig.headers.Authorization = `Bearer ${newAccessToken}`;
        }
        return apiClient(customConfig);
      } catch (refreshError) {
        processQueue(refreshError as Error, null);
        isRefreshing = false;
        store.dispatch(logout());
        store.dispatch(
          showNotification({
            message: 'Your session has expired. Please sign in again.',
            severity: 'error',
          })
        );
        return Promise.reject(refreshError);
      }
    }

    // Handle standard global error notifications (network, 500s, 403 Forbidden)
    if (status === 403) {
      store.dispatch(
        showNotification({
          message: 'Access Denied: You do not have permissions to perform this action.',
          severity: 'error',
        })
      );
    } else if (status && status >= 500) {
      store.dispatch(
        showNotification({
          message: 'System Error: Internal server error. Please try again later.',
          severity: 'error',
        })
      );
    }

    // Attach structured messaging details to error object and reject
    const customError = new Error(errorMessage) as Error & {
      status?: number;
      raw?: AxiosError;
    };
    customError.status = status;
    customError.raw = error;
    
    return Promise.reject(customError);
  }
);
export default apiClient;
