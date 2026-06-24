export interface UpiVerifyRequest {
  upiId: string;
}

export interface UpiVerifyResponse {
  upiId: string;
  score: number;
  status: 'TRUSTED' | 'SAFE' | 'SUSPICIOUS' | 'DANGEROUS';
  reasons: string[];
}

export interface ReputationHistoryItem {
  id: number;
  verifiedEntity: string;
  entityType: string;
  riskScore: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'TRUSTED' | 'SAFE' | 'SUSPICIOUS' | 'DANGEROUS';
  createdAt: string;
}
