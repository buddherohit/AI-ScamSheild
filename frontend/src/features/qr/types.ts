export interface QrScanResponse {
  merchant: string;
  upi: string;
  riskScore: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  recommendation: string;
}

export interface QrVerifyRequest {
  rawText: string;
}
