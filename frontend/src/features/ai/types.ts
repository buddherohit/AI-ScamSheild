export interface SmsAnalysisRequest {
  smsText: string;
}

export interface SmsAnalysisResponse {
  id: number;
  smsText: string;
  riskScore: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  category: string;
  summary: string;
  recommendation: string;
  indicators: string[];
  simpleExplanation: string;
  technicalExplanation: string;
  createdAt: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
