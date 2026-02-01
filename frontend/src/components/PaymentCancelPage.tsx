import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { XCircle, Home, RefreshCw } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent } from './ui/card';

const BOOKING_STORAGE_KEY = 'pendingBooking';

export function PaymentCancelPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const orderId = searchParams.get('orderId') ?? '';

  useEffect(() => {
    sessionStorage.removeItem(BOOKING_STORAGE_KEY);
  }, []);

  return (
    <div className="min-h-screen bg-background">
      <header
        className="text-white py-6 px-4"
        style={{ backgroundColor: '#F59E0B' }}
      >
        <div className="max-w-4xl mx-auto text-center">
          <div className="flex justify-center mb-3">
            <div
              className="w-16 h-16 rounded-full flex items-center justify-center"
              style={{ backgroundColor: 'rgba(255, 255, 255, 0.2)' }}
            >
              <XCircle className="w-10 h-10 text-white" />
            </div>
          </div>
          <h1 className="text-white mb-2">Оплата отменена</h1>
          <p className="text-amber-100">
            Платёж не был завершён. Вы можете попробовать снова или вернуться на главную.
          </p>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-6 space-y-4">
        <Card>
          <CardContent className="p-6 text-center space-y-4">
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
            <p className="text-sm text-muted-foreground">
              Заказ сохранён. Вы можете оплатить его позже из раздела «Мои бронирования».
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
            onClick={() => navigate(-1)}
          >
            <RefreshCw className="w-4 h-4 mr-2" />
            Вернуться назад
          </Button>
        </div>
      </main>
    </div>
  );
}
