import apiClient from './client';

export interface OrderResponse {
  id: string;
  totalPrice: number;
  seatsCount: number;
  tripId: string;
  userId: string;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
  createdAt: string;
}

export interface OrdersPageResponse {
  content: OrderResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export const ordersApi = {
  getMyOrders: async (page = 0, size = 10): Promise<OrdersPageResponse> => {
    const response = await apiClient.get<OrdersPageResponse>('/orders/me', {
      params: { page, size },
    });
    return response.data;
  },
};
