import apiClient from './apiClient';
import { DashboardMetrics, ThreatActivity } from '@/store/slices/dashboardSlice';

export const dashboardService = {
  getDashboardData: async (): Promise<{ data: { metrics: DashboardMetrics; activity: ThreatActivity[] } }> => {
    return apiClient.get('/dashboard/summary');
  },
};
