import { useState, useEffect } from 'react';
import { ArrowLeft, MapPin, Clock, Info, Loader2 } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent } from './ui/card';
import { Badge } from './ui/badge';
import { SeatComponent } from './Seat';
import { Route, Seat } from '../types';
import { inventoryApi, SeatResponse } from '../api/inventory';
import { bookingsApi } from '../api/bookings';
import { getErrorMessage } from '../api/client';

const BOOKING_STORAGE_KEY = 'pendingBooking';

interface SeatSelectionPageProps {
  route: Route;
  onBack: () => void;
  onContinue: (route: Route, selectedSeats: number[]) => void;
}

export function SeatSelectionPage({ route, onBack, onContinue }: SeatSelectionPageProps) {
  const [selectedSeats, setSelectedSeats] = useState<number[]>([]);
  const [seats, setSeats] = useState<Seat[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [bookingInProgress, setBookingInProgress] = useState(false);
  const [bookingError, setBookingError] = useState<string | null>(null);

  useEffect(() => {
    const fetchSeats = async () => {
      try {
        setLoading(true);
        setError(null);
        const seatResponses = await inventoryApi.getSeatsByTripId(route.id);
        
        // Block driver's area (first 2 seats for bus, first 1 for minibus)
        const blockedSeats = route.vehicleType === 'bus' ? [1, 2] : [1];
        
        const mappedSeats: Seat[] = seatResponses.map((seatResponse) => {
          let status: Seat['status'] = 'available';
          
          if (blockedSeats.includes(seatResponse.seatNumber)) {
            status = 'blocked';
          } else {
            // Map backend status to frontend status
            switch (seatResponse.status) {
              case 'AVAILABLE':
                status = 'available';
                break;
              case 'RESERVED':
              case 'SOLD':
                status = 'occupied';
                break;
              case 'CANCELLED':
                status = 'blocked';
                break;
              default:
                status = 'available';
            }
          }
          
          return {
            id: seatResponse.seatNumber,
            status,
          };
        });
        
        setSeats(mappedSeats);
      } catch (err) {
        console.error('Failed to fetch seats', err);
        setError('Не удалось загрузить места. Попробуйте позже.');
      } finally {
        setLoading(false);
      }
    };

    fetchSeats();
  }, [route.id, route.vehicleType]);

  const handleToggleSeat = (seatId: number) => {
    if (selectedSeats.includes(seatId)) {
      setSelectedSeats(selectedSeats.filter(id => id !== seatId));
    } else {
      setSelectedSeats([...selectedSeats, seatId]);
    }
  };

  const totalPrice = selectedSeats.length * route.price;

  const handleBook = async () => {
    if (selectedSeats.length === 0) return;
    setBookingError(null);
    setBookingInProgress(true);
    try {
      const response = await bookingsApi.createBooking({
        tripId: route.id,
        seatsCount: selectedSeats.length,
        seatNumbers: [...selectedSeats].sort((a, b) => a - b),
      });
      if (!response.paymentUrl) {
        setBookingError('Не получена ссылка на оплату. Попробуйте снова.');
        setBookingInProgress(false);
        return;
      }
      sessionStorage.setItem(
        BOOKING_STORAGE_KEY,
        JSON.stringify({ bookingId: response.bookingId, orderId: response.orderId })
      );
      window.location.href = response.paymentUrl;
    } catch (err: unknown) {
      setBookingInProgress(false);
      setBookingError(
        getErrorMessage(err, 'Не удалось забронировать. Войдите в аккаунт и попробуйте снова.')
      );
    }
  };

  // Layout configuration
  const seatsPerRow = route.vehicleType === 'bus' ? 4 : 3;

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
              <h2 className="text-white">Выбор мест</h2>
              <p className="text-sm text-blue-100">
                {route.from.nameRu} → {route.to.nameRu}
              </p>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-4 space-y-4">
        {/* Route Info */}
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between mb-3">
              <Badge variant="secondary">
                {route.vehicleType === 'bus' ? 'Автобус' : 'Маршрутка'}
              </Badge>
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <Clock className="w-4 h-4" />
                {route.duration}
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <div className="text-sm text-muted-foreground mb-1">Отправление</div>
                {route.departureDate && (
                  <div className="text-xs text-muted-foreground">{route.departureDate}</div>
                )}
                <div className="flex items-center gap-2">
                  <MapPin className="w-4 h-4" style={{ color: '#2563EB' }} />
                  <span>{route.departureTime}</span>
                </div>
              </div>
              <div>
                <div className="text-sm text-muted-foreground mb-1">Прибытие</div>
                {route.arrivalDate && (
                  <div className="text-xs text-muted-foreground">{route.arrivalDate}</div>
                )}
                <div className="flex items-center gap-2">
                  <MapPin className="w-4 h-4" style={{ color: '#10B981' }} />
                  <span>{route.arrivalTime}</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Legend */}
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-1 mb-2">
              <Info className="w-4 h-4" style={{ color: '#2563EB' }} />
              <span className="text-sm">Легенда:</span>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-2 text-sm">
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded" style={{ backgroundColor: '#10B981' }} />
                <span>Свободно</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded opacity-50" style={{ backgroundColor: '#9CA3AF' }} />
                <span>Занято</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded" style={{ backgroundColor: '#2563EB' }} />
                <span>Выбрано</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded opacity-50" style={{ backgroundColor: '#EF4444' }} />
                <span>Водитель</span>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Seat Map */}
        <Card>
          <CardContent className="p-4">
            {loading ? (
              <div className="flex justify-center py-12">
                <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
              </div>
            ) : error ? (
              <div className="text-center py-8">
                <p className="text-red-500 mb-4">{error}</p>
                <Button variant="outline" onClick={() => window.location.reload()}>
                  Повторить
                </Button>
              </div>
            ) : (
              <>
                <div className="mb-4 text-center">
                  <div 
                    className="inline-block px-4 py-2 rounded-lg text-sm"
                    style={{ backgroundColor: '#2563EB20', color: '#2563EB' }}
                  >
                    🚗 Водитель
                  </div>
                </div>
                
                <div 
                  className="grid gap-2 max-w-md mx-auto"
                  style={{ 
                    gridTemplateColumns: `repeat(${seatsPerRow}, 1fr)`,
                  }}
                >
                  {seats.map(seat => (
                    <SeatComponent
                      key={seat.id}
                      seat={seat}
                      onToggle={handleToggleSeat}
                      isSelected={selectedSeats.includes(seat.id)}
                    />
                  ))}
                </div>
              </>
            )}
          </CardContent>
        </Card>

        {/* Selected Seats Summary */}
        {selectedSeats.length > 0 && (
          <Card 
            className="sticky bottom-4"
            style={{ backgroundColor: '#2563EB', color: 'white' }}
          >
            <CardContent className="p-4">
              <div className="flex items-center justify-between mb-2">
                <div>
                  <div className="text-sm opacity-90">Выбрано мест: {selectedSeats.length}</div>
                  <div className="text-sm opacity-90">
                    Места: {[...selectedSeats].sort((a, b) => a - b).join(', ')}
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-sm opacity-90">Итого:</div>
                  <div className="text-2xl">{totalPrice} BYN</div>
                </div>
              </div>
              {bookingError && (
                <p className="text-sm bg-white/20 text-white rounded p-2 mb-2">
                  {bookingError}
                </p>
              )}
              <Button 
                className="w-full bg-white hover:bg-gray-100"
                style={{ color: '#2563EB' }}
                onClick={handleBook}
                disabled={bookingInProgress}
              >
                {bookingInProgress ? (
                  <>
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    Бронирование…
                  </>
                ) : (
                  'Забронировать'
                )}
              </Button>
            </CardContent>
          </Card>
        )}
      </main>
    </div>
  );
}
