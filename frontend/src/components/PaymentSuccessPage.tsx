import { useSearchParams, useNavigate, useEffect, useState } from 'react';
import { CheckCircle, Home, ClipboardList } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent } from './ui/card';

const BOOKING_STORAGE_KEY = 'pendingBooking';

export function PaymentSuccessPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const orderIdFromUrl = searchParams.get('orderId') ?? '';
  const [bookingId, setBookingId] = useState<string | null>(null);

  useEffect(() => {
    try {
      const raw = sessionStorage.getItem(BOOKING_STORAGE_KEY);
      if (raw) {
        const data = JSON.parse(raw) as { bookingId?: string; orderId?: string };
        if (data.bookingId) setBookingId(data.bookingId);
        sessionStorage.removeItem(BOOKING_STORAGE_KEY);
      }
    } catch {
      sessionStorage.removeItem(BOOKING_STORAGE_KEY);
    }
  }, []);

  const orderId = orderIdFromUrl;

  return (
    <div className="min-h-screen bg-background">
      <header
        className="text-white py-6 px-4"
        style={{ backgroundColor: '#10B981' }}
      >
        <div className="max-w-4xl mx-auto text-center">
          <div className="flex justify-center mb-3">
            <div
              className="w-16 h-16 rounded-full flex items-center justify-center"
              style={{ backgroundColor: 'rgba(255, 255, 255, 0.2)' }}
            >
              <CheckCircle className="w-10 h-10 text-white" />
            </div>
          </div>
          <h1 className="text-white mb-2">Оплата прошла успешно!</h1>
          <p className="text-green-100">
            Платёж по заказу принят. Бронирование подтверждено.
          </p>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-6 space-y-4">
        <Card>
          <CardContent className="p-6 text-center space-y-4">
            {(orderId || bookingId) && (
              <div className="space-y-3">
                {bookingId && (
                  <div>
                    <div className="text-sm text-muted-foreground mb-1">
                      Номер бронирования
                    </div>
                    <div className="text-2xl font-mono" style={{ color: '#2563EB' }}>
                      {bookingId}
                    </div>
                  </div>
                )}
                {orderId && (
                  <div>
                    <div className="text-sm text-muted-foreground mb-1">
                      Номер заказа
                    </div>
                    <div className="text-xl font-mono text-muted-foreground">
                      {orderId}
                    </div>
                  </div>
                )}
              </div>
            )}
            <p className="text-sm text-muted-foreground">
              Детали заказа и билет доступны в разделе «Мои бронирования».
            </p>
          </CardContent>
        </Card>

        <div className="flex flex-col sm:flex-row gap-3 justify-center">
          <Button
            className="flex-1 sm:flex-initial"
            style={{ backgroundColor: '#2563EB' }}
            onClick={() => navigate('/')}
          >
            <Home className="w-4 h-4 mr-2" />
            На главную
          </Button>
          <Button
            variant="outline"
            className="flex-1 sm:flex-initial"
            onClick={() => navigate('/account')}
          >
            <ClipboardList className="w-4 h-4 mr-2" />
            Мои бронирования
          </Button>
        </div>
      </main>
    </div>
  );
}
