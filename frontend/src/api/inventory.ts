import apiClient from './client';

export interface SeatResponse {
  id: string;
  seatNumber: number;
  status: 'AVAILABLE' | 'RESERVED' | 'SOLD' | 'CANCELLED';
}

export const inventoryApi = {
  getSeatsByTripId: async (tripId: string): Promise<SeatResponse[]> => {
    const response = await apiClient.get<SeatResponse[]>(`/inventory/trips/${tripId}/seats`);
    return response.data;
  },
};
