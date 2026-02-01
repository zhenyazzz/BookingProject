import { useState } from 'react';
import { ArrowLeft, CreditCard, Lock, Shield } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Route, Passenger } from '../types';

interface PaymentPageProps {
  route: Route;
  seats: number[];
  passenger: Passenger;
  onBack: () => void;
  onPaymentComplete: (bookingId: string) => void;
}

export function PaymentPage({ route, seats, passenger, onBack, onPaymentComplete }: PaymentPageProps) {
  const [cardNumber, setCardNumber] = useState('');
  const [cardExpiry, setCardExpiry] = useState('');
  const [cardCvv, setCardCvv] = useState('');
  const [cardName, setCardName] = useState('');
  const [processing, setProcessing] = useState(false);

  const totalPrice = seats.length * route.price;

  const formatCardNumber = (value: string) => {
    const cleaned = value.replace(/\s/g, '');
    const chunks = cleaned.match(/.{1,4}/g) || [];
    return chunks.join(' ').substr(0, 19); // 16 digits + 3 spaces
  };

  const formatExpiry = (value: string) => {
    const cleaned = value.replace(/\D/g, '');
    if (cleaned.length >= 2) {
      return cleaned.substr(0, 2) + '/' + cleaned.substr(2, 2);
    }
    return cleaned;
  };

  const handleCardNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatCardNumber(e.target.value);
    setCardNumber(formatted);
  };

  const handleExpiryChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatExpiry(e.target.value);
    setCardExpiry(formatted);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setProcessing(true);

    // Simulate payment processing
    setTimeout(() => {
      const bookingId = 'BK' + Date.now();
      onPaymentComplete(bookingId);
    }, 2000);
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header 
        className="text-white py-4 px-4 sticky top-0 z-10"
        style={{ backgroundColor: '#2563EB' }}
      >
        <div className="max-w-4xl mx-auto">
          <div className="flex items-center gap-3">
            <Button
              variant="ghost"
              size="sm"
              onClick={onBack}
              className="text-white hover:bg-blue-600 -ml-2"
              disabled={processing}
            >
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <div className="flex-1">
              <h2 className="text-white">Оплата</h2>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-4 space-y-4">
        {/* Security Banner */}
        <div 
          className="p-4 rounded-lg flex items-center gap-3"
          style={{ backgroundColor: '#10B98120' }}
        >
          <Shield className="w-6 h-6" style={{ color: '#10B981' }} />
          <div className="flex-1">
            <div className="text-sm">
              Безопасная оплата через защищенное соединение
            </div>
            <div className="text-xs text-muted-foreground">
              Ваши платежные данные защищены и не сохраняются на наших серверах
            </div>
          </div>
        </div>

        <div className="grid md:grid-cols-[1fr,350px] gap-4">
          {/* Payment Form */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <CreditCard className="w-5 h-5" style={{ color: '#2563EB' }} />
                Данные карты
              </CardTitle>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <Label htmlFor="cardNumber">Номер карты</Label>
                  <Input
                    id="cardNumber"
                    value={cardNumber}
                    onChange={handleCardNumberChange}
                    placeholder="1234 5678 9012 3456"
                    maxLength={19}
                    required
                    disabled={processing}
                  />
                </div>

                <div>
                  <Label htmlFor="cardName">Имя владельца карты</Label>
                  <Input
                    id="cardName"
                    value={cardName}
                    onChange={(e) => setCardName(e.target.value)}
                    placeholder="IVAN IVANOV"
                    required
                    disabled={processing}
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="cardExpiry">Срок действия</Label>
                    <Input
                      id="cardExpiry"
                      value={cardExpiry}
                      onChange={handleExpiryChange}
                      placeholder="MM/YY"
                      maxLength={5}
                      required
                      disabled={processing}
                    />
                  </div>
                  <div>
                    <Label htmlFor="cardCvv" className="flex items-center gap-1">
                      CVV
                      <Lock className="w-3 h-3" />
                    </Label>
                    <Input
                      id="cardCvv"
                      type="password"
                      value={cardCvv}
                      onChange={(e) => setCardCvv(e.target.value.replace(/\D/g, '').substr(0, 3))}
                      placeholder="123"
                      maxLength={3}
                      required
                      disabled={processing}
                    />
                  </div>
                </div>

                <div 
                  className="p-3 rounded text-xs"
                  style={{ backgroundColor: '#2563EB10', color: '#1E40AF' }}
                >
                  💳 Принимаются карты: Visa, MasterCard, Белкарт
                </div>

                <Button 
                  type="submit"
                  className="w-full h-12"
                  style={{ backgroundColor: '#10B981' }}
                  disabled={processing}
                >
                  {processing ? (
                    <span className="flex items-center gap-2">
                      <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                      Обработка платежа...
                    </span>
                  ) : (
                    `Оплатить ${totalPrice} BYN`
                  )}
                </Button>
              </form>
            </CardContent>
          </Card>

          {/* Order Summary */}
          <div>
            <Card>
              <CardHeader>
                <CardTitle>К оплате</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="space-y-2">
                  <div className="text-sm text-muted-foreground">Маршрут</div>
                  <div>{route.from.nameRu} → {route.to.nameRu}</div>
                </div>

                <div className="space-y-2">
                  <div className="text-sm text-muted-foreground">Пассажир</div>
                  <div>{passenger.firstName} {passenger.lastName}</div>
                  <div className="text-sm">{passenger.phone}</div>
                </div>

                <div className="space-y-2">
                  <div className="text-sm text-muted-foreground">Места</div>
                  <div>{seats.join(', ')}</div>
                </div>

                <div 
                  className="p-3 rounded-lg"
                  style={{ backgroundColor: '#2563EB20' }}
                >
                  <div className="text-sm text-muted-foreground mb-1">Итого</div>
                  <div className="text-3xl" style={{ color: '#2563EB' }}>
                    {totalPrice} BYN
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </div>
  );
}
