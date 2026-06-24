import apiClient from './apiClient';
import { UserSession } from '@/store/slices/sessionSlice';

export const sessionService = {
  getSessions: async (): Promise<{ data: UserSession[] }> => {
    return apiClient.get('/auth/sessions');
  },
  logoutSession: async (sessionId: number): Promise<void> => {
    return apiClient.post(`/auth/sessions/${sessionId}/logout`);
  },
  logoutAllSessions: async (): Promise<void> => {
    return apiClient.post('/auth/sessions/logout-all');
  },
};
