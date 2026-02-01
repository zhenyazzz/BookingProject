import { MapPin, Calendar, Armchair, Info, Eye, X } from 'lucide-react';
import { Card, CardContent } from './ui/card';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Booking } from '../types';

interface BookingCardProps {
  booking: Booking;
  onViewDetails: (booking: Booking) => void;
  onCancel: (bookingId: string) => void;
}

export function BookingCard({ booking, onViewDetails, onCancel }: BookingCardProps) {
  const getStatusInfo = () => {
    switch (booking.status) {
      case 'CONFIRMED':
        return { label: 'Подтверждено', color: '#10B981', bg: '#10B98120' };
      case 'PENDING':
        return { label: 'Ожидает оплаты', color: '#F59E0B', bg: '#F59E0B20' };
      case 'CANCELLED':
        return { label: 'Отменено', color: '#EF4444', bg: '#EF444420' };
      default:
        return { label: 'Неизвестно', color: '#9CA3AF', bg: '#9CA3AF20' };
    }
  };

  const statusInfo = getStatusInfo();
  const bookingDate = new Date(booking.bookingDate);
  const canCancel = booking.status === 'CONFIRMED' || booking.status === 'PENDING';

  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardContent className="p-4">
        <div className="space-y-3">
          {/* Header */}
          <div className="flex items-start justify-between">
            <div>
              <div className="text-sm text-muted-foreground mb-1">
                Заказ #{booking.id}
              </div>
              <div className="text-xs text-muted-foreground">
                {bookingDate.toLocaleDateString('ru-RU')} {bookingDate.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}
              </div>
            </div>
            <Badge 
              style={{ 
                backgroundColor: statusInfo.bg,
                color: statusInfo.color,
              }}
            >
              {statusInfo.label}
            </Badge>
          </div>

          {/* Route Info */}
          <div>
            <div className="flex items-center gap-2 mb-2">
              <MapPin className="w-4 h-4" style={{ color: '#2563EB' }} />
              <span className="text-sm">
                {booking.route.from.nameRu} → {booking.route.to.nameRu}
              </span>
            </div>
            <div className="grid grid-cols-2 gap-2 text-sm">
              <div className="flex items-center gap-2">
                <Calendar className="w-4 h-4 text-muted-foreground" />
                <span>
                  {booking.route.departureDate && `${booking.route.departureDate} `}
                  {booking.route.departureTime}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <Armchair className="w-4 h-4 text-muted-foreground" />
                <span>Места: {booking.seats.join(', ')}</span>
              </div>
            </div>
          </div>

          {/* Price and Actions */}
          <div className="flex items-center justify-between pt-2 border-t">
            <div className="text-xl" style={{ color: '#2563EB' }}>
              {booking.totalPrice} BYN
            </div>
            <div className="flex gap-2">
              <Button
                size="sm"
                variant="outline"
                onClick={() => onViewDetails(booking)}
              >
                <Eye className="w-4 h-4 mr-1" />
                Билет
              </Button>
              {canCancel && (
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => onCancel(booking.id)}
                  style={{ color: '#EF4444', borderColor: '#EF4444' }}
                >
                  <X className="w-4 h-4 mr-1" />
                  Отменить
                </Button>
              )}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
