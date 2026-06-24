import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export type AlertSeverity = 'success' | 'info' | 'warning' | 'error';

interface ToastNotification {
  open: boolean;
  message: string;
  severity: AlertSeverity;
}

interface UIState {
  themeMode: 'light' | 'dark';
  sidebarOpen: boolean;
  notification: ToastNotification;
}

const getStoredThemeMode = (): 'light' | 'dark' => {
  const mode = localStorage.getItem('themeMode');
  if (mode === 'light' || mode === 'dark') {
    return mode;
  }
  // Check system preferences
  if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return 'dark';
  }
  return 'light';
};

const initialState: UIState = {
  themeMode: getStoredThemeMode(),
  sidebarOpen: true,
  notification: {
    open: false,
    message: '',
    severity: 'info',
  },
};

const uiSlice = createSlice({
  name: 'ui',
  initialState,
  reducers: {
    toggleThemeMode(state) {
      state.themeMode = state.themeMode === 'light' ? 'dark' : 'light';
      localStorage.setItem('themeMode', state.themeMode);
    },
    setThemeMode(state, action: PayloadAction<'light' | 'dark'>) {
      state.themeMode = action.payload;
      localStorage.setItem('themeMode', action.payload);
    },
    toggleSidebar(state) {
      state.sidebarOpen = !state.sidebarOpen;
    },
    setSidebarOpen(state, action: PayloadAction<boolean>) {
      state.sidebarOpen = action.payload;
    },
    showNotification(state, action: PayloadAction<{ message: string; severity?: AlertSeverity }>) {
      state.notification = {
        open: true,
        message: action.payload.message,
        severity: action.payload.severity || 'info',
      };
    },
    hideNotification(state) {
      state.notification.open = false;
    },
  },
});

export const {
  toggleThemeMode,
  setThemeMode,
  toggleSidebar,
  setSidebarOpen,
  showNotification,
  hideNotification,
} = uiSlice.actions;

export default uiSlice.reducer;
