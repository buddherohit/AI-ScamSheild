import apiClient from './apiClient';

export interface UserProfile {
  id: string;
  name: string;
  email: string;
  role: string;
  emailVerified: boolean;
  avatarUrl?: string;
}

export const userService = {
  getProfile: async (): Promise<{ data: UserProfile }> => {
    return apiClient.get('/users/me');
  },
  updateProfile: async (data: Partial<UserProfile>): Promise<{ data: UserProfile }> => {
    return apiClient.put('/users/me', data);
  },
  uploadAvatar: async (file: File): Promise<{ data: { avatarUrl: string } }> => {
    const formData = new FormData();
    formData.append('avatar', file);
    return apiClient.post('/users/me/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};
