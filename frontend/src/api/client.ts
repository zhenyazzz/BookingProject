import axios from 'axios';
import keycloak from '../auth/keycloak';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080', // API Gateway URL
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  async (config) => {
    // Публичные endpoints доступны без авторизации — не отправляем токен,
    // иначе шлюз валидирует его и при истёкшем/невалидном токене вернёт 401
    const publicPaths = ['/routes', '/trips', '/inventory'];
    const isPublicPath = publicPaths.some(path => config.url?.includes(path));

    if (!isPublicPath) {
      try {
        if (keycloak.authenticated) {
          await keycloak.updateToken(30);
        }
      } catch (error) {
        console.error('Failed to refresh token', error);
      }
      if (keycloak.token) {
        config.headers.Authorization = `Bearer ${keycloak.token}`;
      }
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Extract user-facing error message from axios error (e.g. 400/401/500).
 * Handles JSON body with `message` or plain string body.
 */
export function getErrorMessage(
  err: unknown,
  fallback: string = 'Произошла ошибка. Попробуйте снова.'
): string {
  if (!err || typeof err !== 'object' || !('response' in err)) return fallback;
  const res = (err as { response?: { data?: unknown } }).response;
  if (!res?.data) return fallback;
  const d = res.data;
  if (typeof d === 'string' && d.trim()) return d.trim();
  if (typeof d === 'object' && d !== null && 'message' in d && typeof (d as { message: unknown }).message === 'string')
    return (d as { message: string }).message;
  return fallback;
}

export default apiClient;
