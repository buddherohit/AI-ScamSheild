import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface Notification {
  id: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
  type: 'security' | 'system' | 'account' | 'alert';
}

interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  isOpen: boolean;
}

const initialState: NotificationState = {
  notifications: [],
  unreadCount: 0,
  isOpen: false,
};

const notificationSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {
    setNotifications(state, action: PayloadAction<Notification[]>) {
      state.notifications = action.payload;
      state.unreadCount = action.payload.filter(n => !n.read).length;
    },
    markAsRead(state, action: PayloadAction<string>) {
      const notification = state.notifications.find(n => n.id === action.payload);
      if (notification && !notification.read) {
        notification.read = true;
        state.unreadCount = Math.max(0, state.unreadCount - 1);
      }
    },
    markAllAsRead(state) {
      state.notifications.forEach(n => { n.read = true; });
      state.unreadCount = 0;
    },
    toggleNotificationDrawer(state) {
      state.isOpen = !state.isOpen;
    },
    setNotificationDrawerOpen(state, action: PayloadAction<boolean>) {
      state.isOpen = action.payload;
    }
  },
});

export const {
  setNotifications,
  markAsRead,
  markAllAsRead,
  toggleNotificationDrawer,
  setNotificationDrawerOpen
} = notificationSlice.actions;

export default notificationSlice.reducer;
