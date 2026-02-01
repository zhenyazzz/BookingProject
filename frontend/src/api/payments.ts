import apiClient from './client';

export type PaymentStatus = 'PENDING' | 'SUCCEEDED' | 'FAILED' | 'CANCELLED';

export interface PaymentListItemResponse {
  id: string;
  orderId: string;
  amount: number;
  currency: string;
  status: PaymentStatus;
  createdAt: string;
  paidAt: string | null;
}

export const paymentsApi = {
  getPaymentsByOrderIds: async (orderIds: string[]): Promise<PaymentListItemResponse[]> => {
    if (!orderIds.length) return [];
    const params = new URLSearchParams();
    orderIds.forEach((id) => params.append('orderIds', id));
    const response = await apiClient.get<PaymentListItemResponse[]>(`/payments?${params.toString()}`);
    return response.data;
  },
};
