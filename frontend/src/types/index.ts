export interface City {
  id: string;
  name: string;
  nameRu: string;
}

export interface Route {
  id: string;
  from: City;
  to: City;
  /** Display date e.g. "2 фев" or "02.02.2026" */
  departureDate?: string;
  departureTime: string;
  /** Display date for arrival */
  arrivalDate?: string;
  arrivalTime: string;
  duration: string;
  price: number;
  vehicleType: 'bus' | 'minibus';
  availableSeats: number;
  totalSeats: number;
}

export interface Seat {
  id: number;
  status: 'available' | 'occupied' | 'selected' | 'blocked';
}

export interface Passenger {
  firstName: string;
  lastName: string;
  phone: string;
  email?: string;
}

export interface Booking {
  id: string;
  route: Route;
  seats: number[];
  passenger: Passenger;
  totalPrice: number;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
  bookingDate: string;
  qrCode?: string;
}

export interface PopularRoute {
  id: string;
  from: string;
  to: string;
  price: number;
  image: string;
}
