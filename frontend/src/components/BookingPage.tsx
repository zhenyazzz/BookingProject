import { useState, useEffect } from 'react';
import { ArrowLeft, User, Phone, Mail, MapPin, Calendar, Armchair } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Separator } from './ui/separator';
import { CountdownTimer } from './CountdownTimer';
import { Route, Passenger } from '../types';
import apiClient from '../api/client';
import { getErrorMessage } from '../api/client';
import { useAuth } from '../context/AuthProvider';
import { bookingsApi } from '../api/bookings';

const BOOKING_STORAGE_KEY = 'pendingBooking';

interface BookingPageProps {
  route: Route;
  seats: number[];
  onBack: () => void;
  onContinueToPayment: (passenger: Passenger) => void;
}

export function BookingPage({ route, seats, onBack, onContinueToPayment }: BookingPageProps) {
  const { token, userProfile } = useAuth();
  const [formData, setFormData] = useState<Passenger>({
    firstName: '',
    lastName: '',
    phone: '',
    email: '',
  });
  const [errors, setErrors] = useState<Partial<Record<keyof Passenger, string>>>({});
  const [processing, setProcessing] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // Pre-fill passenger form from current user profile
  useEffect(() => {
    const fillFromProfile = async () => {
      if (!token) return;
      try {
        const response = await apiClient.get('/users/me');
        const user = response.data;
        setFormData(prev => ({
          ...prev,
          firstName: user.firstName ?? userProfile?.firstName ?? prev.firstName,
          lastName: user.lastName ?? userProfile?.lastName ?? prev.lastName,
          phone: user.phoneNumber ?? (userProfile as { attributes?: Record<string, string[]> })?.attributes?.phoneNumber?.[0] ?? prev.phone,
          email: user.email ?? userProfile?.email ?? prev.email ?? '',
        }));
      } catch {
        // Fallback to Keycloak profile when backend fails (e.g. 401)
        if (userProfile) {
          const attrs = (userProfile as { attributes?: Record<string, string[]> }).attributes;
          setFormData(prev => ({
            ...prev,
            firstName: userProfile.firstName ?? prev.firstName,
            lastName: userProfile.lastName ?? prev.lastName,
            phone: attrs?.phoneNumber?.[0] ?? prev.phone,
            email: userProfile.email ?? prev.email ?? '',
          }));
        }
      }
    };
    fillFromProfile();
  }, [token, userProfile]);

  const totalPrice = seats.length * route.price;

  const handleTimerExpire = () => {
    alert('Время бронирования истекло. Пожалуйста, начните заново.');
    onBack();
  };

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof Passenger, string>> = {};

    if (!formData.firstName.trim()) {
      newErrors.firstName = 'Введите имя';
    }
    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Введите фамилию';
    }
    if (!formData.phone.trim()) {
      newErrors.phone = 'Введите номер телефона';
    } else if (!/^\+?[0-9]{9,15}$/.test(formData.phone.replace(/[\s-]/g, ''))) {
      newErrors.phone = 'Некорректный номер телефона';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitError(null);
    if (!validateForm()) return;

    setProcessing(true);
    try {
      const response = await bookingsApi.createBooking({
        tripId: route.id,
        seatsCount: seats.length,
        seatNumbers: seats,
      });

      if (!response.paymentUrl) {
        setSubmitError('Не получена ссылка на оплату. Попробуйте снова.');
        setProcessing(false);
        return;
      }

      sessionStorage.setItem(
        BOOKING_STORAGE_KEY,
        JSON.stringify({
          bookingId: response.bookingId,
          orderId: response.orderId,
        })
      );
      window.location.href = response.paymentUrl;
    } catch (err: unknown) {
      setProcessing(false);
      setSubmitError(
        getErrorMessage(err, 'Не удалось создать бронирование. Проверьте авторизацию и попробуйте снова.')
      );
    }
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
            >
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <div className="flex-1">
              <h2 className="text-white">Оформление заказа</h2>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-4 space-y-4">
        {/* Timer */}
        <CountdownTimer minutes={15} onExpire={handleTimerExpire} />

        <div className="grid md:grid-cols-[1fr,400px] gap-4">
          {/* Passenger Form */}
          <div>
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <User className="w-5 h-5" style={{ color: '#2563EB' }} />
                  Данные пассажира
                </CardTitle>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div>
                    <Label htmlFor="firstName">Имя *</Label>
                    <Input
                      id="firstName"
                      value={formData.firstName}
                      onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                      placeholder="Иван"
                      className={errors.firstName ? 'border-destructive' : ''}
                    />
                    {errors.firstName && (
                      <p className="text-sm text-destructive mt-1">{errors.firstName}</p>
                    )}
                  </div>

                  <div>
                    <Label htmlFor="lastName">Фамилия *</Label>
                    <Input
                      id="lastName"
                      value={formData.lastName}
                      onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                      placeholder="Иванов"
                      className={errors.lastName ? 'border-destructive' : ''}
                    />
                    {errors.lastName && (
                      <p className="text-sm text-destructive mt-1">{errors.lastName}</p>
                    )}
                  </div>

                  <div>
                    <Label htmlFor="phone" className="flex items-center gap-2">
                      <Phone className="w-4 h-4" />
                      Телефон *
                    </Label>
                    <Input
                      id="phone"
                      type="tel"
                      value={formData.phone}
                      onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                      placeholder="+375 29 123-45-67"
                      className={errors.phone ? 'border-destructive' : ''}
                    />
                    {errors.phone && (
                      <p className="text-sm text-destructive mt-1">{errors.phone}</p>
                    )}
                  </div>

                  <div>
                    <Label htmlFor="email" className="flex items-center gap-2">
                      <Mail className="w-4 h-4" />
                      Email (опционально)
                    </Label>
                    <Input
                      id="email"
                      type="email"
                      value={formData.email}
                      onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                      placeholder="ivanov@example.com"
                    />
                    <p className="text-xs text-muted-foreground mt-1">
                      На email будет отправлен электронный билет
                    </p>
                  </div>

                  {submitError && (
                    <p className="text-sm text-destructive bg-destructive/10 p-3 rounded-md">
                      {submitError}
                    </p>
                  )}
                  <Button 
                    type="submit"
                    className="w-full h-12"
                    style={{ backgroundColor: '#2563EB' }}
                    disabled={processing}
                  >
                    {processing ? 'Создание бронирования…' : 'Перейти к оплате'}
                  </Button>
                </form>
              </CardContent>
            </Card>
          </div>

          {/* Order Summary */}
          <div>
            <Card>
              <CardHeader>
                <CardTitle>Детали заказа</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {/* Route Info */}
                <div>
                  <div className="flex items-start gap-2 mb-2">
                    <MapPin className="w-4 h-4 mt-1" style={{ color: '#2563EB' }} />
                    <div className="flex-1">
                      <div className="text-sm text-muted-foreground">Маршрут</div>
                      <div>{route.from.nameRu}</div>
                      <div className="text-sm text-muted-foreground">→</div>
                      <div>{route.to.nameRu}</div>
                    </div>
                  </div>

                  <div className="flex items-start gap-2 mb-2">
                    <Calendar className="w-4 h-4 mt-1" style={{ color: '#2563EB' }} />
                    <div className="flex-1">
                      <div className="text-sm text-muted-foreground">Отправление</div>
                      {route.departureDate && (
                        <div className="text-xs text-muted-foreground">{route.departureDate}</div>
                      )}
                      <div>{route.departureTime}</div>
                    </div>
                  </div>

                  <div className="flex items-start gap-2">
                    <Armchair className="w-4 h-4 mt-1" style={{ color: '#2563EB' }} />
                    <div className="flex-1">
                      <div className="text-sm text-muted-foreground">Места</div>
                      <div>{seats.join(', ')}</div>
                    </div>
                  </div>
                </div>

                <Separator />

                {/* Price Breakdown */}
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span>Цена за место</span>
                    <span>{route.price} BYN</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span>Количество мест</span>
                    <span>× {seats.length}</span>
                  </div>
                  <Separator />
                  <div className="flex justify-between items-center">
                    <span>Итого:</span>
                    <span className="text-2xl" style={{ color: '#2563EB' }}>
                      {totalPrice} BYN
                    </span>
                  </div>
                </div>

                {/* Info */}
                <div 
                  className="p-3 rounded-lg text-sm"
                  style={{ backgroundColor: '#10B98120', color: '#065F46' }}
                >
                  ✓ Бесплатная отмена за 24 часа до отправления
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </div>
  );
}
