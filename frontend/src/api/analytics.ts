import apiClient from './client';

export interface RevenueStatsResponse {
  totalRevenue: number;
  averageOrderValue: number;
  orderCount: number;
  dailyRevenue: Record<string, { revenue: number; orderCount: number }>;
}

export interface OrderStatsResponse {
  totalOrders: number;
  ordersByStatus: Record<string, number>;
}

export interface BookingStatsResponse {
  totalBookings: number;
  bookingsByStatus: Record<string, number>;
  conversionRate: number | null;
  dailyStats: Record<string, { bookingCount: number; totalSeats: number }>;
}

export interface PopularTripResponse {
  tripId: string;
  bookingCount: number;
  totalSeats: number;
}

export interface RouteStatsResponse {
  routeId: string;
  fromCity: string;
  toCity: string;
  tripCount: number;
}

export interface PaymentStatsResponse {
  totalSucceededAmount: number;
  totalCount: number;
  countByStatus: Record<string, number>;
}

const dateParams = (startDate?: string, endDate?: string) => {
  const params = new URLSearchParams();
  if (startDate) params.append('startDate', startDate);
  if (endDate) params.append('endDate', endDate);
  return params.toString();
};

export const analyticsApi = {
  getOrderRevenue: async (startDate?: string, endDate?: string): Promise<RevenueStatsResponse> => {
    const query = dateParams(startDate, endDate);
    const response = await apiClient.get<RevenueStatsResponse>(`/orders/analytics/revenue${query ? `?${query}` : ''}`);
    return response.data;
  },

  getOrderStats: async (startDate?: string, endDate?: string): Promise<OrderStatsResponse> => {
    const query = dateParams(startDate, endDate);
    const response = await apiClient.get<OrderStatsResponse>(`/orders/analytics/stats${query ? `?${query}` : ''}`);
    return response.data;
  },

  getBookingStats: async (startDate?: string, endDate?: string): Promise<BookingStatsResponse> => {
    const query = dateParams(startDate, endDate);
    const response = await apiClient.get<BookingStatsResponse>(`/booking/analytics/stats${query ? `?${query}` : ''}`);
    return response.data;
  },

  getPopularTrips: async (startDate?: string, endDate?: string, limit = 10): Promise<PopularTripResponse[]> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    params.append('limit', limit.toString());
    const response = await apiClient.get<PopularTripResponse[]>(`/booking/analytics/popular-trips?${params.toString()}`);
    return response.data;
  },

  getPopularRoutes: async (startDate?: string, endDate?: string, limit = 10): Promise<RouteStatsResponse[]> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    params.append('limit', limit.toString());
    const response = await apiClient.get<RouteStatsResponse[]>(`/routes/analytics/popular?${params.toString()}`);
    return response.data;
  },

  getPaymentStats: async (startDate?: string, endDate?: string): Promise<PaymentStatsResponse> => {
    const query = dateParams(startDate, endDate);
    const response = await apiClient.get<PaymentStatsResponse>(`/payments/analytics/stats${query ? `?${query}` : ''}`);
    return response.data;
  },
};
