import { CheckCircle, Download, Mail, Home, QrCode, MapPin, Calendar, Armchair, User } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent } from './ui/card';
import { Separator } from './ui/separator';
import { Route, Passenger } from '../types';

interface ConfirmationPageProps {
  bookingId: string;
  route: Route;
  seats: number[];
  passenger: Passenger;
  onBackToHome: () => void;
}

export function ConfirmationPage({ bookingId, route, seats, passenger, onBackToHome }: ConfirmationPageProps) {
  const totalPrice = seats.length * route.price;
  const qrCodeData = `booking:${bookingId}`;
  const qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(qrCodeData)}`;

  const handleDownload = () => {
    alert('Билет будет скачан в формате PDF');
  };

  const handleSendEmail = () => {
    if (passenger.email) {
      alert(`Билет будет отправлен на ${passenger.email}`);
    } else {
      alert('Email не указан');
    }
  };

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
            Ваше бронирование подтверждено
          </p>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-6 space-y-4">
        {/* Booking ID */}
        <Card>
          <CardContent className="p-4 text-center">
            <div className="text-sm text-muted-foreground mb-1">Номер заказа</div>
            <div className="text-2xl" style={{ color: '#2563EB' }}>
              {bookingId}
            </div>
          </CardContent>
        </Card>

        <div className="grid md:grid-cols-[1fr,280px] gap-4">
          {/* Ticket Details */}
          <div className="space-y-4">
            {/* QR Code */}
            <Card>
              <CardContent className="p-6">
                <div className="flex flex-col items-center">
                  <div className="mb-3 flex items-center gap-2 text-muted-foreground">
                    <QrCode className="w-5 h-5" />
                    <span>QR-код билета</span>
                  </div>
                  <div 
                    className="w-48 h-48 rounded-lg flex items-center justify-center"
                    style={{ backgroundColor: '#F3F4F6' }}
                  >
                    <img 
                      src={qrCodeUrl} 
                      alt="QR Code"
                      className="w-44 h-44"
                    />
                  </div>
                  <p className="text-xs text-muted-foreground mt-3 text-center">
                    Покажите этот QR-код водителю при посадке
                  </p>
                </div>
              </CardContent>
            </Card>

            {/* Route Details */}
            <Card>
              <CardContent className="p-4 space-y-3">
                <h3 className="flex items-center gap-2">
                  <MapPin className="w-5 h-5" style={{ color: '#2563EB' }} />
                  Детали рейса
                </h3>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <div className="text-sm text-muted-foreground">Откуда</div>
                    <div>{route.from.nameRu}</div>
                    {route.departureDate && (
                      <div className="text-xs text-muted-foreground">{route.departureDate}</div>
                    )}
                    <div className="text-sm" style={{ color: '#2563EB' }}>
                      {route.departureTime}
                    </div>
                  </div>
                  <div>
                    <div className="text-sm text-muted-foreground">Куда</div>
                    <div>{route.to.nameRu}</div>
                    {route.arrivalDate && (
                      <div className="text-xs text-muted-foreground">{route.arrivalDate}</div>
                    )}
                    <div className="text-sm" style={{ color: '#10B981' }}>
                      {route.arrivalTime}
                    </div>
                  </div>
                </div>

                <Separator />

                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <div className="text-muted-foreground mb-1">Длительность</div>
                    <div className="flex items-center gap-1">
                      <Calendar className="w-4 h-4" />
                      {route.duration}
                    </div>
                  </div>
                  <div>
                    <div className="text-muted-foreground mb-1">Места</div>
                    <div className="flex items-center gap-1">
                      <Armchair className="w-4 h-4" />
                      {seats.join(', ')}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Passenger Details */}
            <Card>
              <CardContent className="p-4">
                <h3 className="flex items-center gap-2 mb-3">
                  <User className="w-5 h-5" style={{ color: '#2563EB' }} />
                  Данные пассажира
                </h3>
                <div className="space-y-2 text-sm">
                  <div>
                    <span className="text-muted-foreground">Имя: </span>
                    {passenger.firstName} {passenger.lastName}
                  </div>
                  <div>
                    <span className="text-muted-foreground">Телефон: </span>
                    {passenger.phone}
                  </div>
                  {passenger.email && (
                    <div>
                      <span className="text-muted-foreground">Email: </span>
                      {passenger.email}
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Actions */}
          <div className="space-y-3">
            <Card>
              <CardContent className="p-4">
                <div className="mb-3">
                  <div className="text-sm text-muted-foreground mb-1">Оплачено</div>
                  <div className="text-2xl" style={{ color: '#10B981' }}>
                    {totalPrice} BYN
                  </div>
                </div>
                
                <div className="space-y-2">
                  <Button 
                    className="w-full"
                    style={{ backgroundColor: '#2563EB' }}
                    onClick={handleDownload}
                  >
                    <Download className="w-4 h-4 mr-2" />
                    Скачать билет
                  </Button>
                  
                  <Button 
                    className="w-full"
                    variant="outline"
                    onClick={handleSendEmail}
                  >
                    <Mail className="w-4 h-4 mr-2" />
                    Отправить на email
                  </Button>
                  
                  <Button 
                    className="w-full"
                    variant="outline"
                    onClick={onBackToHome}
                  >
                    <Home className="w-4 h-4 mr-2" />
                    На главную
                  </Button>
                </div>
              </CardContent>
            </Card>

            {/* Info Box */}
            <Card>
              <CardContent className="p-4">
                <div 
                  className="p-3 rounded text-sm space-y-2"
                  style={{ backgroundColor: '#10B98120' }}
                >
                  <div className="flex items-start gap-2">
                    <span className="text-green-600">ℹ️</span>
                    <p className="text-sm text-muted-foreground">
                      Билет также отправлен на указанный номер телефона SMS-сообщением
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>

        {/* Important Information */}
        <Card>
          <CardContent className="p-4">
            <h3 className="mb-3">Важная информация</h3>
            <ul className="space-y-2 text-sm text-muted-foreground">
              <li>• Прибудьте на автовокзал за 15 минут до отправления</li>
              <li>• При посадке необходимо предъявить QR-код билета и документ, удостоверяющий личность</li>
              <li>• Бесплатная отмена возможна не позднее чем за 24 часа до отправления</li>
              <li>• При опоздании билет не возвращается</li>
              <li>• Разрешен провоз ручной клади весом до 10 кг</li>
            </ul>
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
