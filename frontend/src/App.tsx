import { useState } from 'react';
import { Routes, Route, useNavigate, useLocation } from 'react-router-dom';
import { HomePage } from './components/HomePage';
import { RouteListPage } from './components/RouteListPage';
import { SeatSelectionPage } from './components/SeatSelectionPage';
import { BookingPage } from './components/BookingPage';
import { PaymentPage } from './components/PaymentPage';
import { PaymentSuccessPage } from './components/PaymentSuccessPage';
import { PaymentCancelPage } from './components/PaymentCancelPage';
import { ConfirmationPage } from './components/ConfirmationPage';
import { AccountPage } from './components/AccountPage';
import { AdminDashboard } from './components/AdminDashboard';
import { MyBookingsPage } from './components/MyBookingsPage';
import { ProfilePage } from './components/ProfilePage';
import AdminRoute from './components/AdminRoute';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import PrivateRoute from './components/PrivateRoute';
import { City, Route as RouteType, Passenger, Booking } from './types';

export default function App() {
  const navigate = useNavigate();
  const location = useLocation();

  const [selectedRoute, setSelectedRoute] = useState<RouteType | null>(null);
  const [selectedSeats, setSelectedSeats] = useState<number[]>([]);
  const [passengerData, setPassengerData] = useState<Passenger | null>(null);
  const [bookingId, setBookingId] = useState<string>('');
  const [lastSearchQuery, setLastSearchQuery] = useState<string>('');

  const handleSearch = (from: City, to: City, date: string) => {
    navigate(`/routes?from=${from.nameRu}&to=${to.nameRu}&date=${date}`, {
        state: { from, to, date }
    });
  };


  const handleSelectSeats = (route: RouteType, searchQuery: string) => {
    setSelectedRoute(route);
    setLastSearchQuery(searchQuery);
    navigate('/seats');
  };

  const handleContinueFromSeats = (route: RouteType, seats: number[]) => {
    setSelectedRoute(route);
    setSelectedSeats(seats);
    navigate('/booking');
  };

  const handleContinueToPayment = (passenger: Passenger) => {
    setPassengerData(passenger);
    navigate('/payment');
  };

  const handlePaymentComplete = (bookingIdGenerated: string) => {
    setBookingId(bookingIdGenerated);
    navigate('/confirmation');
  };

  const handleViewTicket = (booking: Booking) => {
      // Logic for viewing ticket
       navigate('/confirmation');
  };

  return (
    <div className="size-full">
      <Routes>
        <Route path="/" element={
          <HomePage 
            onSearch={handleSearch}
            onProfileClick={() => navigate('/account')}
          />
        } />
        
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route path="/routes" element={
          <RouteListPageWrapper 
             onBack={() => navigate('/')}
             onSelectSeats={handleSelectSeats}
          />
        } />

        <Route path="/seats" element={
          selectedRoute ? (
            <SeatSelectionPage
              route={selectedRoute}
              onBack={() => navigate(`/routes${lastSearchQuery}`)}
              onContinue={handleContinueFromSeats}
            />
          ) : <HomePage onSearch={handleSearch} onProfileClick={() => navigate('/account')} />
        } />

        <Route path="/booking" element={
          selectedRoute ? (
            <BookingPage
              route={selectedRoute}
              seats={selectedSeats}
              onBack={() => navigate('/seats')}
              onContinueToPayment={handleContinueToPayment}
            />
          ) : <HomePage onSearch={handleSearch} onProfileClick={() => navigate('/account')} />
        } />

        <Route path="/payment" element={
           selectedRoute && passengerData ? (
            <PaymentPage
              route={selectedRoute}
              seats={selectedSeats}
              passenger={passengerData}
              onBack={() => navigate('/booking')}
              onPaymentComplete={handlePaymentComplete}
            />
          ) : <HomePage onSearch={handleSearch} onProfileClick={() => navigate('/account')} />
        } />

        <Route path="/payment/success" element={<PaymentSuccessPage />} />
        <Route path="/payment/cancel" element={<PaymentCancelPage />} />

        <Route path="/confirmation" element={
           selectedRoute && passengerData ? (
            <ConfirmationPage
              bookingId={bookingId}
              route={selectedRoute}
              seats={selectedSeats}
              passenger={passengerData}
              onBackToHome={() => navigate('/')}
            />
          ) : <HomePage onSearch={handleSearch} onProfileClick={() => navigate('/account')} />
        } />

        <Route element={<PrivateRoute />}>
          <Route path="/account" element={
            <AccountPage
              onBack={() => navigate('/')}
              onMyBookings={() => navigate('/mybookings')}
              onProfile={() => navigate('/profile')}
              onAnalytics={() => navigate('/admin')}
            />
          } />
          <Route path="/admin" element={<AdminRoute />}>
            <Route index element={<AdminDashboard onBack={() => navigate('/account')} />} />
          </Route>
          
          <Route path="/mybookings" element={
            <MyBookingsPage
              onBack={() => navigate('/account')}
              onViewTicket={handleViewTicket}
            />
          } />
          
          <Route path="/profile" element={
            <ProfilePage
              onBack={() => navigate('/account')}
            />
          } />
        </Route>
      </Routes>
    </div>
  );
}

// Wrapper to handle URL params for RouteListPage
function RouteListPageWrapper({ onBack, onSelectSeats }: { onBack: () => void, onSelectSeats: (route: RouteType, searchQuery: string) => void }) {
    const location = useLocation();
    const searchParams = new URLSearchParams(location.search);
    const from = searchParams.get('from') || '';
    const to = searchParams.get('to') || '';
    const date = searchParams.get('date') || '';

    return (
        <RouteListPage
            fromCity={from}
            toCity={to}
            date={date}
            onBack={onBack}
            onSelectSeats={(route) => onSelectSeats(route, location.search)}
        />
    );
}
