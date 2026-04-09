// API-related types
export interface PaymentResult {
  success: boolean;
  transactionId?: string;
  subscriptionId?: string;
  customerId?: string;
  error?: string;
  campaignTitle?: string;
  magicLinkToken?: string; // Plain token from ephemeral storage (2-minute window)
}

export interface ApiResponse<T> {
  data: T;
  success: boolean;
  error?: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  hasMore: boolean;
}
