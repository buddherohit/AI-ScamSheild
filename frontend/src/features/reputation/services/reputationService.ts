import { apiClient } from '@/services/apiClient';
import { UpiVerifyRequest, UpiVerifyResponse, ReputationHistoryItem } from '../types';

export const reputationService = {
  verifyUpi: async (data: UpiVerifyRequest): Promise<UpiVerifyResponse> => {
    // apiClient response interceptor unwraps response.data, which returns the ApiResponse object.
    // The ApiResponse has fields: success, message, data.
    const res = await apiClient.post<any, any>('/reputation/verify-upi', data);
    return res.data;
  },

  getHistory: async (): Promise<ReputationHistoryItem[]> => {
    const res = await apiClient.get<any, any>('/reputation/history');
    return res.data;
  },

  getUpiScore: async (upi: string): Promise<UpiVerifyResponse> => {
    const res = await apiClient.get<any, any>(`/reputation/score/${upi}`);
    return res.data;
  },
};

export default reputationService;
