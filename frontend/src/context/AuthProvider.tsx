import React, { createContext, useContext, useEffect, useState } from 'react';
import keycloak from '../auth/keycloak';
import { KeycloakProfile } from 'keycloak-js';

interface AuthContextType {
  isAuthenticated: boolean;
  token: string | undefined;
  userProfile: KeycloakProfile | undefined;
  login: () => void;
  logout: () => void;
  register: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [userProfile, setUserProfile] = useState<KeycloakProfile | undefined>(undefined);
  const [token, setToken] = useState<string | undefined>(undefined);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const initKeycloak = async () => {
      try {
        const authenticated = await keycloak.init({
          onLoad: 'check-sso',
          silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
          pkceMethod: 'S256',
        });

        setIsAuthenticated(authenticated);
        if (authenticated) {
          setToken(keycloak.token ?? undefined);
          const profile = await keycloak.loadUserProfile();
          setUserProfile(profile);
        }

        // Keep React token in sync when Keycloak refreshes (API client uses keycloak.token directly)
        keycloak.onAuthRefreshSuccess = () => setToken(keycloak.token ?? undefined);
        keycloak.onAuthRefreshError = () => setToken(undefined);
        keycloak.onAuthLogout = () => setToken(undefined);
      } catch (error) {
        console.error('Failed to initialize Keycloak', error);
      } finally {
        setLoading(false);
      }
    };

    initKeycloak();
  }, []);

  const login = () => {
    keycloak.login({ scope: 'openid profile email phone' });
  };

  const logout = () => {
    keycloak.logout();
  };

  const register = () => {
    keycloak.register({ scope: 'openid profile email phone' });
  };

  if (loading) {
    return <div>Loading authentication...</div>;
  }

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        token,
        userProfile,
        login,
        logout,
        register,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
