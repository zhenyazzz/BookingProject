import React, { useState, useEffect } from 'react';
import { ArrowLeft } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { analyticsApi } from '../api/analytics';
import type {
  RevenueStatsResponse,
  OrderStatsResponse,
  BookingStatsResponse,
  RouteStatsResponse,
  PopularTripResponse,
  PaymentStatsResponse,
} from '../api/analytics';

interface AdminDashboardProps {
  onBack: () => void;
}

export function AdminDashboard({ onBack }: AdminDashboardProps) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [revenue, setRevenue] = useState<RevenueStatsResponse | null>(null);
  const [orderStats, setOrderStats] = useState<OrderStatsResponse | null>(null);
  const [bookingStats, setBookingStats] = useState<BookingStatsResponse | null>(null);
  const [popularRoutes, setPopularRoutes] = useState<RouteStatsResponse[]>([]);
  const [popularTrips, setPopularTrips] = useState<PopularTripResponse[]>([]);
  const [paymentStats, setPaymentStats] = useState<PaymentStatsResponse | null>(null);

  useEffect(() => {
    const fetchAll = async () => {
      setLoading(true);
      setError(null);
      try {
        const [rev, ord, book, routes, trips, pay] = await Promise.all([
          analyticsApi.getOrderRevenue().catch(() => null),
          analyticsApi.getOrderStats().catch(() => null),
          analyticsApi.getBookingStats().catch(() => null),
          analyticsApi.getPopularRoutes(undefined, undefined, 10).catch(() => []),
          analyticsApi.getPopularTrips(undefined, undefined, 10).catch(() => []),
          analyticsApi.getPaymentStats().catch(() => null),
        ]);
        setRevenue(rev ?? null);
        setOrderStats(ord ?? null);
        setBookingStats(book ?? null);
        setPopularRoutes(routes ?? []);
        setPopularTrips(trips ?? []);
        setPaymentStats(pay ?? null);
      } catch (err) {
        console.error('Failed to fetch analytics', err);
        setError('Не удалось загрузить аналитику.');
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, []);

  return (
    <div className="min-h-screen bg-background">
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
              <h2 className="text-white">Аналитика</h2>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-4 space-y-6">
        {loading && (
          <div className="text-sm text-muted-foreground py-4">Загрузка аналитики...</div>
        )}
        {error && (
          <div className="text-sm text-destructive py-4">{error}</div>
        )}
        {!loading && !error && (
          <>
            {revenue && (
              <Card>
                <CardHeader>
                  <CardTitle>Доход (заказы)</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-2xl" style={{ color: '#2563EB' }}>
                    {Number(revenue.totalRevenue).toFixed(2)} BYN
                  </p>
                  <p className="text-sm text-muted-foreground">
                    Заказов: {revenue.orderCount} · Средний чек: {Number(revenue.averageOrderValue).toFixed(2)} BYN
                  </p>
                </CardContent>
              </Card>
            )}

            {orderStats && (
              <Card>
                <CardHeader>
                  <CardTitle>Статистика заказов</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-xl">Всего: {orderStats.totalOrders}</p>
                  <ul className="mt-2 text-sm space-y-1">
                    {Object.entries(orderStats.ordersByStatus || {}).map(([status, count]) => (
                      <li key={status}>{status}: {count}</li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            )}

            {bookingStats && (
              <Card>
                <CardHeader>
                  <CardTitle>Статистика бронирований</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-xl">Всего: {bookingStats.totalBookings}</p>
                  {bookingStats.conversionRate != null && (
                    <p className="text-sm text-muted-foreground">
                      Конверсия: {(bookingStats.conversionRate * 100).toFixed(1)}%
                    </p>
                  )}
                  <ul className="mt-2 text-sm space-y-1">
                    {Object.entries(bookingStats.bookingsByStatus || {}).map(([status, count]) => (
                      <li key={status}>{status}: {count}</li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            )}

            {paymentStats && (
              <Card>
                <CardHeader>
                  <CardTitle>Статистика платежей</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-xl" style={{ color: '#2563EB' }}>
                    Успешно оплачено: {Number(paymentStats.totalSucceededAmount).toFixed(2)} BYN
                  </p>
                  <p className="text-sm text-muted-foreground">Всего платежей: {paymentStats.totalCount}</p>
                  <ul className="mt-2 text-sm space-y-1">
                    {Object.entries(paymentStats.countByStatus || {}).map(([status, count]) => (
                      <li key={status}>{status}: {count}</li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            )}

            {popularRoutes.length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>Популярные маршруты</CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2">
                    {popularRoutes.map((r) => (
                      <li key={r.routeId} className="flex justify-between text-sm">
                        <span>{r.fromCity} → {r.toCity}</span>
                        <span>{r.tripCount} рейсов</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            )}

            {popularTrips.length > 0 && (
              <Card>
                <CardHeader>
                  <CardTitle>Популярные рейсы</CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2">
                    {popularTrips.map((t) => (
                      <li key={t.tripId} className="flex justify-between text-sm">
                        <span>Рейс {t.tripId.slice(0, 8)}…</span>
                        <span>бронирований: {t.bookingCount}, мест: {t.totalSeats}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            )}
          </>
        )}
      </main>
    </div>
  );
}
