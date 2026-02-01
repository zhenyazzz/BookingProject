import apiClient from './client';
import { Route } from '../types';

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  last: boolean;
  size: number;
  number: number;
}

interface GetTripsParams {
  fromCity: string;
  toCity: string;
  date: string;
  page: number;
  size: number;
}

// Helper to calculate duration between two times
const calculateDuration = (departure: string, arrival: string): string => {
  // Assuming times are in HH:mm format and on the same day or next day
  // This is a simple approximation for display
  const [depH, depM] = departure.split(':').map(Number);
  const [arrH, arrM] = arrival.split(':').map(Number);
  
  let diffMinutes = (arrH * 60 + arrM) - (depH * 60 + depM);
  if (diffMinutes < 0) diffMinutes += 24 * 60; // Next day
  
  const hours = Math.floor(diffMinutes / 60);
  const minutes = diffMinutes % 60;
  
  return `${hours}ч ${minutes > 0 ? `${minutes}м` : ''}`;
};

export const tripsApi = {
  getTrips: async (params: GetTripsParams): Promise<PageResponse<Route>> => {
    const response = await apiClient.get<any>(`/trips`, {
      params: {
        fromCity: params.fromCity,
        toCity: params.toCity,
        date: params.date,
        page: params.page,
        size: params.size,
      },
    });

    const formatDate = (iso?: string) => {
      if (!iso) return undefined;
      try {
        const d = new Date(iso);
        return d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short', year: 'numeric' });
      } catch {
        return undefined;
      }
    };

    // Map backend response to frontend Route type
    const mappedContent = response.data.content.map((trip: any) => {
      const fromCityName = trip.route?.fromCity || '';
      const toCityName = trip.route?.toCity || '';
      const departureTimeStr = trip.departureTime?.split('T')[1]?.substring(0, 5) || '00:00';
      const arrivalTimeStr = trip.arrivalTime?.split('T')[1]?.substring(0, 5) || '00:00';
      
      return {
        id: trip.id,
        from: {
          id: fromCityName,
          name: fromCityName,
          nameRu: fromCityName,
        },
        to: {
          id: toCityName,
          name: toCityName,
          nameRu: toCityName,
        },
        departureDate: formatDate(trip.departureTime),
        departureTime: departureTimeStr,
        arrivalDate: formatDate(trip.arrivalTime),
        arrivalTime: arrivalTimeStr,
        duration: calculateDuration(departureTimeStr, arrivalTimeStr),
        price: Number(trip.price) || 0,
        availableSeats: trip.totalSeats || 0,
        totalSeats: trip.totalSeats || 0,
        vehicleType: trip.busType === 'BUS' ? 'bus' : 'minibus',
      };
    });

    return {
      ...response.data,
      content: mappedContent,
    };
  },
};
