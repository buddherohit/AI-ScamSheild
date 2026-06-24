import apiClient from './apiClient';
import { Notification } from '@/store/slices/notificationSlice';

export const notificationService = {
  getNotifications: async (): Promise<{ data: Notification[] }> => {
    return apiClient.get('/notifications');
  },
  markAsRead: async (id: string): Promise<void> => {
    return apiClient.put(`/notifications/${id}/read`);
  },
  markAllAsRead: async (): Promise<void> => {
    return apiClient.put('/notifications/read-all');
  },
};
