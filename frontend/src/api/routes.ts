import apiClient from './client';

export interface RouteResponse {
  id: string;
  fromCity: string;
  toCity: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface RoutesPageParams {
  fromCity?: string;
  toCity?: string;
  page?: number;
  size?: number;
}

export interface CityResponse {
  name: string;
  nameRu: string;
}

export const routesApi = {
  getRoutes: async (params: RoutesPageParams = {}): Promise<PageResponse<RouteResponse>> => {
    const { fromCity, toCity, page = 0, size = 10 } = params;
    const queryParams = new URLSearchParams();
    
    if (fromCity) queryParams.append('fromCity', fromCity);
    if (toCity) queryParams.append('toCity', toCity);
    queryParams.append('page', page.toString());
    queryParams.append('size', size.toString());
    
    const response = await apiClient.get(`/routes?${queryParams.toString()}`);
    return response.data;
  },

  getCities: async (): Promise<CityResponse[]> => {
    const response = await apiClient.get('/routes/cities');
    return response.data;
  },
};
