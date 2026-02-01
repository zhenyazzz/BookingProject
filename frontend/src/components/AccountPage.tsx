import React, { useEffect, useState } from 'react';
import { ArrowLeft, User, Receipt, LogOut, BarChart3 } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent } from './ui/card';
import { useAuth } from '../context/AuthProvider';
import apiClient from '../api/client';
import { isAdmin } from './AdminRoute';

interface AccountPageProps {
  onBack: () => void;
  onMyBookings: () => void;
  onProfile: () => void;
  onAnalytics?: () => void;
}

export function AccountPage({ onBack, onMyBookings, onProfile, onAnalytics }: AccountPageProps) {
  const { logout, userProfile, token } = useAuth();
  const [userInfo, setUserInfo] = useState({
    firstName: userProfile?.firstName || 'Пользователь',
    lastName: userProfile?.lastName || '',
    email: userProfile?.email || '—'
  });

  useEffect(() => {
    if (!token) return;
    const fetchInfo = async () => {
      try {
        const response = await apiClient.get('/users/me');
        if (response.data) {
          setUserInfo({
            firstName: response.data.firstName || userProfile?.firstName || 'Пользователь',
            lastName: response.data.lastName || userProfile?.lastName || '',
            email: response.data.email || userProfile?.email || ''
          });
        }
      } catch (err) {
        console.error("Could not fetch user info", err);
      }
    };
    fetchInfo();
  }, [token, userProfile]);

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header 
        className="text-white py-4 px-4"
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
              <h2 className="text-white">Личный кабинет</h2>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-6">
        {/* User Info */}
        <Card className="mb-6">
          <CardContent className="p-6 text-center">
            <div 
              className="w-20 h-20 rounded-full mx-auto mb-4 flex items-center justify-center text-3xl"
              style={{ backgroundColor: '#2563EB20' }}
            >
              👤
            </div>
            <h2>{userInfo.firstName} {userInfo.lastName}</h2>
            <p className="text-sm text-muted-foreground">{userInfo.email}</p>
          </CardContent>
        </Card>

        {/* Menu Options */}
        <div className="space-y-3">
          <Card 
            className="cursor-pointer hover:shadow-md transition-shadow"
            onClick={onMyBookings}
          >
            <CardContent className="p-4">
              <div className="flex items-center gap-4">
                <div 
                  className="w-12 h-12 rounded-lg flex items-center justify-center"
                  style={{ backgroundColor: '#2563EB20' }}
                >
                  <Receipt className="w-6 h-6" style={{ color: '#2563EB' }} />
                </div>
                <div className="flex-1">
                  <h3>Мои бронирования</h3>
                  <p className="text-sm text-muted-foreground">
                    Активные и прошлые поездки
                  </p>
                </div>
                <span className="text-2xl">→</span>
              </div>
            </CardContent>
          </Card>

          <Card 
            className="cursor-pointer hover:shadow-md transition-shadow"
            onClick={onProfile}
          >
            <CardContent className="p-4">
              <div className="flex items-center gap-4">
                <div 
                  className="w-12 h-12 rounded-lg flex items-center justify-center"
                  style={{ backgroundColor: '#10B98120' }}
                >
                  <User className="w-6 h-6" style={{ color: '#10B981' }} />
                </div>
                <div className="flex-1">
                  <h3>Профиль</h3>
                  <p className="text-sm text-muted-foreground">
                    Личные данные и настройки
                  </p>
                </div>
                <span className="text-2xl">→</span>
              </div>
            </CardContent>
          </Card>

          {isAdmin() && onAnalytics && (
            <Card 
              className="cursor-pointer hover:shadow-md transition-shadow"
              onClick={onAnalytics}
            >
              <CardContent className="p-4">
                <div className="flex items-center gap-4">
                  <div 
                    className="w-12 h-12 rounded-lg flex items-center justify-center"
                    style={{ backgroundColor: '#7C3AED20' }}
                  >
                    <BarChart3 className="w-6 h-6" style={{ color: '#7C3AED' }} />
                  </div>
                  <div className="flex-1">
                    <h3>Аналитика</h3>
                    <p className="text-sm text-muted-foreground">
                      Статистика заказов, бронирований и платежей
                    </p>
                  </div>
                  <span className="text-2xl">→</span>
                </div>
              </CardContent>
            </Card>
          )}

          <Card 
            className="cursor-pointer hover:shadow-md transition-shadow hover:bg-red-50"
            onClick={logout}
          >
            <CardContent className="p-4">
              <div className="flex items-center gap-4">
                <div 
                  className="w-12 h-12 rounded-lg flex items-center justify-center"
                  style={{ backgroundColor: '#FECACA' }}
                >
                  <LogOut className="w-6 h-6 text-red-600" />
                </div>
                <div className="flex-1">
                  <h3 className="text-red-600">Выйти</h3>
                  <p className="text-sm text-muted-foreground">
                    Выход из аккаунта
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  );
}
