import { apiClient } from '@/services/apiClient';
import { SmsAnalysisRequest, SmsAnalysisResponse, PaginatedResponse } from '../types';

export const aiService = {
  analyzeSms: async (data: SmsAnalysisRequest): Promise<SmsAnalysisResponse> => {
    const res = await apiClient.post<any, any>('/ai/analyze-sms', data);
    return res.data;
  },

  getHistory: async (search?: string, page = 0, size = 10): Promise<PaginatedResponse<SmsAnalysisResponse>> => {
    const params = new URLSearchParams();
    if (search) params.append('search', search);
    params.append('page', page.toString());
    params.append('size', size.toString());

    const res = await apiClient.get<any, any>(`/ai/history?${params.toString()}`);
    return res.data;
  },

  getAnalysisDetails: async (id: number): Promise<SmsAnalysisResponse> => {
    const res = await apiClient.get<any, any>(`/ai/analysis/${id}`);
    return res.data;
  },

  deleteAnalysis: async (id: number): Promise<void> => {
    await apiClient.delete(`/ai/analysis/${id}`);
  },

  getFraudCategories: async (): Promise<string[]> => {
    const res = await apiClient.get<any, any>('/ai/fraud-categories');
    return res.data;
  },
};

export default aiService;
