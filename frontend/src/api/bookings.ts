import apiClient from './client';

export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED';

export interface CreateBookingRequest {
  tripId: string;
  seatsCount: number;
  seatNumbers: number[];
}

export interface CreateBookingResponse {
  bookingId: string;
  orderId: string;
  tripId: string;
  seatsCount: number;
  status: BookingStatus;
  paymentUrl: string;
  reservationExpiresAt: string;
  createdAt: string;
}

export interface BookingResponse {
  id: string;
  userId: string;
  tripId: string;
  seatsCount: number;
  status: BookingStatus;
  createdAt: string;
}

export interface BookingsPageResponse {
  content: BookingResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export const bookingsApi = {
  createBooking: async (body: CreateBookingRequest): Promise<CreateBookingResponse> => {
    const response = await apiClient.post<CreateBookingResponse>('/booking', body);
    return response.data;
  },

  getMyBookings: async (page = 0, size = 10): Promise<BookingsPageResponse> => {
    const response = await apiClient.get<BookingsPageResponse>('/booking/me', {
      params: { page, size },
    });
    return response.data;
  },

  cancelBooking: async (bookingId: string): Promise<BookingResponse> => {
    const response = await apiClient.delete<BookingResponse>(`/booking/cancel/${bookingId}`);
    return response.data;
  },
};
