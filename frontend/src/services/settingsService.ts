import apiClient from './apiClient';
import { SettingsState } from '@/store/slices/settingsSlice';

export const settingsService = {
  getSettings: async (): Promise<{ data: SettingsState }> => {
    return apiClient.get('/settings');
  },
  updateSettings: async (settings: Partial<SettingsState>): Promise<{ data: SettingsState }> => {
    return apiClient.put('/settings', settings);
  },
};
