import { apiClient } from '@/services/apiClient';
import { QrScanResponse, QrVerifyRequest } from '../types';

export const qrService = {
  scanQrImage: async (file: File): Promise<QrScanResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    
    const res = await apiClient.post<any, any>('/qr/scan', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return res.data;
  },

  verifyQrText: async (data: QrVerifyRequest): Promise<QrScanResponse> => {
    const res = await apiClient.post<any, any>('/qr/verify', data);
    return res.data;
  },

  getHistory: async (): Promise<QrScanResponse[]> => {
    const res = await apiClient.get<any, any>('/qr/history');
    return res.data;
  },
};

export default qrService;
