import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthProvider';
import keycloak from '../auth/keycloak';

export function isAdmin(): boolean {
  if (!keycloak.authenticated) return false;
  return keycloak.hasRealmRole('admin') || keycloak.hasRealmRole('ADMIN');
}

const AdminRoute: React.FC = () => {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  if (!isAdmin()) {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
};

export default AdminRoute;
