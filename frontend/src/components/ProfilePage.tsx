import React, { useState, useEffect } from 'react';
import { ArrowLeft, User, Mail, Phone, Calendar, Save } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Switch } from './ui/switch';
import apiClient from '../api/client';
import { useAuth } from '../context/AuthProvider';
import { ordersApi, type OrderResponse } from '../api/orders';
import { paymentsApi, type PaymentListItemResponse } from '../api/payments';

interface ProfilePageProps {
  onBack: () => void;
}

export function ProfilePage({ onBack }: ProfilePageProps) {
  const { userProfile } = useAuth();
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    dateOfBirth: '', // Not currently synced with backend
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // Fetch user profile from backend
    const fetchProfile = async () => {
      try {
        const response = await apiClient.get('/users/me');
        const user = response.data;
        setFormData(prev => ({
          ...prev,
          firstName: user.firstName || userProfile?.firstName || '',
          lastName: user.lastName || userProfile?.lastName || '',
          email: user.email || userProfile?.email || '',
          phone: user.phoneNumber || (userProfile as { attributes?: Record<string, string[]> })?.attributes?.phoneNumber?.[0] || '',
          // Preserve local dateOfBirth or handle if backend adds it
        }));
      } catch (error) {
        console.error('Failed to fetch profile', error);
        // Fallback to Keycloak profile if backend fetch fails
        if (userProfile) {
           const attrs = (userProfile as { attributes?: Record<string, string[]> }).attributes;
           setFormData(prev => ({
            ...prev,
             firstName: userProfile.firstName || '',
             lastName: userProfile.lastName || '',
             email: userProfile.email || '',
             phone: attrs?.phoneNumber?.[0] || prev.phone,
           }));
        }
      }
    };

    fetchProfile();
  }, [userProfile]);

  const [notifications, setNotifications] = useState({
    emailNotifications: true,
    smsNotifications: true,
    promotions: false,
  });

  const [saved, setSaved] = useState(false);

  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [paymentsByOrderId, setPaymentsByOrderId] = useState<Record<string, PaymentListItemResponse>>({});
  const [historyLoading, setHistoryLoading] = useState(true);
  const [historyError, setHistoryError] = useState<string | null>(null);

  useEffect(() => {
    const fetchOrderAndPaymentHistory = async () => {
      setHistoryLoading(true);
      setHistoryError(null);
      try {
        const ordersPage = await ordersApi.getMyOrders(0, 50);
        const orderList = ordersPage.content;
        setOrders(orderList);
        if (orderList.length > 0) {
          const orderIds = orderList.map((o) => o.id);
          const payments = await paymentsApi.getPaymentsByOrderIds(orderIds);
          const byOrderId: Record<string, PaymentListItemResponse> = {};
          payments.forEach((p) => {
            byOrderId[p.orderId] = p;
          });
          setPaymentsByOrderId(byOrderId);
        }
      } catch (err) {
        console.error('Failed to fetch order/payment history', err);
        setHistoryError('Не удалось загрузить историю заказов и платежей.');
      } finally {
        setHistoryLoading(false);
      }
    };
    fetchOrderAndPaymentHistory();
  }, []);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await apiClient.put('/users/me', {
        firstName: formData.firstName,
        lastName: formData.lastName,
        phoneNumber: formData.phone,
      });
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (error) {
      console.error('Failed to update profile', error);
    } finally {
      setLoading(false);
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
              <h2 className="text-white">Профиль</h2>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-4 space-y-4">
        {/* Success Message */}
        {saved && (
          <div 
            className="p-4 rounded-lg text-center"
            style={{ backgroundColor: '#10B98120', color: '#065F46' }}
          >
            ✓ Изменения сохранены
          </div>
        )}

        {/* Personal Info */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="w-5 h-5" style={{ color: '#2563EB' }} />
              Личные данные
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSave} className="space-y-4">
              <div className="grid md:grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="firstName">Имя</Label>
                  <Input
                    id="firstName"
                    value={formData.firstName}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                  />
                </div>
                <div>
                  <Label htmlFor="lastName">Фамилия</Label>
                  <Input
                    id="lastName"
                    value={formData.lastName}
                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  />
                </div>
              </div>

              <div>
                <Label htmlFor="email" className="flex items-center gap-2">
                  <Mail className="w-4 h-4" />
                  Email
                </Label>
                <Input
                  id="email"
                  type="email"
                  value={formData.email}
                  disabled // Email usually managed by auth provider
                  className="bg-gray-100"
                />
              </div>

              <div>
                <Label htmlFor="phone" className="flex items-center gap-2">
                  <Phone className="w-4 h-4" />
                  Телефон
                </Label>
                <Input
                  id="phone"
                  type="tel"
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                />
              </div>

              <div>
                <Label htmlFor="dateOfBirth" className="flex items-center gap-2">
                  <Calendar className="w-4 h-4" />
                  Дата рождения
                </Label>
                <Input
                  id="dateOfBirth"
                  type="date"
                  value={formData.dateOfBirth}
                  onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
                />
              </div>

              <Button 
                type="submit"
                className="w-full"
                style={{ backgroundColor: '#2563EB' }}
                disabled={loading}
              >
                <Save className="w-4 h-4 mr-2" />
                {loading ? 'Сохранение...' : 'Сохранить изменения'}
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* Notification Settings */}
        <Card>
          <CardHeader>
            <CardTitle>Настройки уведомлений</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <div className="text-sm">Email уведомления</div>
                <div className="text-xs text-muted-foreground">
                  Получать информацию о бронировании на email
                </div>
              </div>
              <Switch
                checked={notifications.emailNotifications}
                onCheckedChange={(checked) => 
                  setNotifications({ ...notifications, emailNotifications: checked })
                }
              />
            </div>

            <div className="flex items-center justify-between">
              <div>
                <div className="text-sm">SMS уведомления</div>
                <div className="text-xs text-muted-foreground">
                  Получать SMS о статусе бронирования
                </div>
              </div>
              <Switch
                checked={notifications.smsNotifications}
                onCheckedChange={(checked) => 
                  setNotifications({ ...notifications, smsNotifications: checked })
                }
              />
            </div>

            <div className="flex items-center justify-between">
              <div>
                <div className="text-sm">Акции и специальные предложения</div>
                <div className="text-xs text-muted-foreground">
                  Получать информацию о скидках и акциях
                </div>
              </div>
              <Switch
                checked={notifications.promotions}
                onCheckedChange={(checked) => 
                  setNotifications({ ...notifications, promotions: checked })
                }
              />
            </div>
          </CardContent>
        </Card>

        {/* Order and Payment History from backend */}
        <Card>
          <CardHeader>
            <CardTitle>История заказов и платежей</CardTitle>
          </CardHeader>
          <CardContent>
            {historyLoading && (
              <div className="text-sm text-muted-foreground py-4">Загрузка...</div>
            )}
            {historyError && (
              <div className="text-sm text-destructive py-4">{historyError}</div>
            )}
            {!historyLoading && !historyError && orders.length === 0 && (
              <div className="text-sm text-muted-foreground py-4">Пока нет заказов.</div>
            )}
            {!historyLoading && !historyError && orders.length > 0 && (
              <div className="space-y-3">
                {orders.map((order) => {
                  const payment = paymentsByOrderId[order.id];
                  const dateStr = order.createdAt ? new Date(order.createdAt).toLocaleDateString('ru-RU') : '';
                  const amount = payment?.amount ?? order.totalPrice;
                  const currency = payment?.currency?.toUpperCase() || 'BYN';
                  const status = payment?.status ?? order.status;
                  return (
                    <div key={order.id} className="flex justify-between items-center py-2 border-b last:border-0">
                      <div>
                        <div className="text-sm">Заказ от {dateStr}</div>
                        <div className="text-xs text-muted-foreground">
                          Статус: {status}
                        </div>
                      </div>
                      <div style={{ color: '#2563EB' }}>{Number(amount)} {currency}</div>
                    </div>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
