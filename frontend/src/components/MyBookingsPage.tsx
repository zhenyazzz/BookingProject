import React, { useState, useEffect } from 'react';
import { ArrowLeft } from 'lucide-react';
import { Button } from './ui/button';
import { BookingCard } from './BookingCard';
import { Booking } from '../types';
import { bookingsApi, type BookingResponse } from '../api/bookings';

interface MyBookingsPageProps {
  onBack: () => void;
  onViewTicket: (booking: Booking) => void;
}

function mapBookingResponseToBooking(b: BookingResponse): Booking {
  return {
    id: b.id,
    route: {
      id: b.tripId,
      from: { id: b.tripId, name: 'Рейс', nameRu: `Рейс ${b.tripId.slice(0, 8)}` },
      to: { id: b.tripId, name: 'Рейс', nameRu: `Рейс ${b.tripId.slice(0, 8)}` },
      departureTime: '—',
      arrivalTime: '—',
      duration: '—',
      price: 0,
      vehicleType: 'bus',
      availableSeats: 0,
      totalSeats: b.seatsCount,
    },
    seats: Array.from({ length: b.seatsCount }, (_, i) => i + 1),
    passenger: { firstName: '', lastName: '', phone: '' },
    totalPrice: 0,
    status: b.status,
    bookingDate: b.createdAt,
  };
}

export function MyBookingsPage({ onBack, onViewTicket }: MyBookingsPageProps) {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterStatus, setFilterStatus] = useState<'all' | 'CONFIRMED' | 'PENDING' | 'CANCELLED'>('all');

  useEffect(() => {
    const fetchBookings = async () => {
      setLoading(true);
      setError(null);
      try {
        const page = await bookingsApi.getMyBookings(0, 50);
        setBookings(page.content.map(mapBookingResponseToBooking));
      } catch (err) {
        console.error('Failed to fetch bookings', err);
        setError('Не удалось загрузить бронирования.');
      } finally {
        setLoading(false);
      }
    };
    fetchBookings();
  }, []);

  const handleCancelBooking = async (bookingId: string) => {
    if (!confirm('Вы уверены, что хотите отменить бронирование?')) return;
    try {
      await bookingsApi.cancelBooking(bookingId);
      setBookings(bookings.map(b =>
        b.id === bookingId ? { ...b, status: 'CANCELLED' as const } : b
      ));
    } catch (err) {
      console.error('Failed to cancel booking', err);
    }
  };

  const filteredBookings = bookings.filter(booking => 
    filterStatus === 'all' || booking.status === filterStatus
  );

  const activeBookings = filteredBookings.filter(b => b.status === 'CONFIRMED');
  const pendingBookings = filteredBookings.filter(b => b.status === 'PENDING');
  const cancelledBookings = filteredBookings.filter(b => b.status === 'CANCELLED');

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header 
        className="text-white py-4 px-4 sticky top-0 z-10"
        style={{ backgroundColor: '#2563EB' }}
      >
        <div className="max-w-6xl mx-auto">
          <div className="flex items-center gap-3">
            <Button
              variant="ghost"
              size="sm"
              onClick={onBack}
              className="text-white hover:bg-blue-600 -ml-2"
            >
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <div className="flex-1">
              <h2 className="text-white">Мои бронирования</h2>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-4">
        {loading && (
          <div className="text-sm text-muted-foreground py-4">Загрузка бронирований...</div>
        )}
        {error && (
          <div className="text-sm text-destructive py-4">{error}</div>
        )}
        {!loading && !error && (
          <>
        {/* Filter Buttons */}
        <div className="mb-4 flex gap-2 overflow-x-auto pb-2">
          {[
            { value: 'all', label: 'Все', count: bookings.length },
            { value: 'CONFIRMED', label: 'Активные', count: bookings.filter(b => b.status === 'CONFIRMED').length },
            { value: 'PENDING', label: 'Ожидают оплаты', count: bookings.filter(b => b.status === 'PENDING').length },
            { value: 'CANCELLED', label: 'Отменённые', count: bookings.filter(b => b.status === 'CANCELLED').length },
          ].map(option => (
            <Button
              key={option.value}
              variant={filterStatus === option.value ? 'default' : 'outline'}
              size="sm"
              onClick={() => setFilterStatus(option.value as any)}
              style={filterStatus === option.value ? { backgroundColor: '#2563EB' } : {}}
              className="flex-shrink-0"
            >
              {option.label} ({option.count})
            </Button>
          ))}
        </div>

        {/* Bookings List */}
        <div className="space-y-3">
          {filteredBookings.length > 0 ? (
            filteredBookings.map(booking => (
              <BookingCard
                key={booking.id}
                booking={booking}
                onViewDetails={onViewTicket}
                onCancel={handleCancelBooking}
              />
            ))
          ) : (
            <div className="text-center py-12">
              <div className="text-4xl mb-3">📋</div>
              <p className="text-muted-foreground">
                {filterStatus === 'all' 
                  ? 'У вас пока нет бронирований' 
                  : 'Нет бронирований с выбранным статусом'}
              </p>
              <Button 
                className="mt-4"
                style={{ backgroundColor: '#2563EB' }}
                onClick={onBack}
              >
                Забронировать билет
              </Button>
            </div>
          )}
        </div>
          </>
        )}
      </main>
    </div>
  );
}
