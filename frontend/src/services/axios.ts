import axios from 'axios';
import { useAuthStore } from '../store/authStore';

/**
 * Axios 인스턴스 생성
 *
 * - baseURL: Vite 환경변수 VITE_API_BASE_URL에서 가져옴 (기본값: /api)
 * - timeout: 10초
 * - 요청/응답 인터셉터를 통한 JWT 자동 주입 및 에러 처리
 */
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ── 요청 인터셉터 ──────────────────────────────────────────────────
apiClient.interceptors.request.use(
  (config) => {
    // authStore에서 accessToken을 읽어 Authorization 헤더에 자동 주입
    const token = useAuthStore.getState().accessToken;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 토큰 갱신 재시도 플래그 (무한루프 방지)
let isRefreshing = false;
let failedQueue: { resolve: (v: any) => void; reject: (e: any) => void }[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((p) => (error ? p.reject(error) : p.resolve(token)));
  failedQueue = [];
};

// ── 응답 인터셉터 ──────────────────────────────────────────────────
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      const storedRefreshToken = useAuthStore.getState().refreshToken;

      // refreshToken도 없으면 즉시 로그아웃
      if (!storedRefreshToken) {
        useAuthStore.getState().logout();
        window.location.href = '/auth';
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // 이미 갱신 중이면 큐에 추가하고 대기
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // refreshToken으로 새 accessToken 발급
        const { data } = await axios.post(
          `${apiClient.defaults.baseURL}/member/refresh`,
          { refreshToken: storedRefreshToken },
          { headers: { 'Content-Type': 'application/json' } }
        );
        const newAccessToken: string = data.accessToken;
        const newRefreshToken: string = data.refreshToken;

        // 스토어 및 원본 요청 헤더 업데이트
        useAuthStore.getState().setTokens(newAccessToken, newRefreshToken);
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

        processQueue(null, newAccessToken);
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        useAuthStore.getState().logout();
        window.location.href = '/auth';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    if (error.response?.status === 403) {
      console.warn('[API] 접근 권한 없음');
    } else if (error.response?.status >= 500) {
      console.error('[API] 서버 오류:', error.response.data);
    } else if (error.request) {
      console.error('[API] 서버 응답 없음 - 백엔드 미가동 또는 네트워크 오류');
    }

    return Promise.reject(error);
  }
);

export default apiClient;
