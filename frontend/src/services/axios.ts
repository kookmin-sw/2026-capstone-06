import axios from 'axios';
import { useAuthStore } from '../store/authStore';

/**
 * Axios 인스턴스 생성
 *
 * - baseURL: Vite 환경변수 VITE_API_ENDPOINT에서 가져옴
 * - 개발: http://localhost:8081
 * - 프로덕션: 프록시(/api)를 통해 Vercel에서 EC2로 포워딩
 * - timeout: 10초
 * - 요청/응답 인터셉터를 통한 JWT 자동 주입 및 에러 처리
 */
const getBaseURL = () => {
  const apiEndpoint = import.meta.env.VITE_API_ENDPOINT;
  if (apiEndpoint) {
    return apiEndpoint;
  }
  // 기본값
  return import.meta.env.VITE_API_BASE_URL || '/api';
};

const apiClient = axios.create({
  baseURL: getBaseURL(),
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ── JWT 토큰 파싱 유틸 ──────────────────────────────────────────────
/** JWT 토큰을 파싱해서 payload 반환 */
const parseJWT = (token: string): any => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    console.error('[JWT] Token parse error:', e);
    return null;
  }
};

/** 토큰 만료 여부 확인 (5분 여유) */
const isTokenExpiringSoon = (token: string, bufferMinutes: number = 5): boolean => {
  const payload = parseJWT(token);
  if (!payload || !payload.exp) return false;
  
  const expiresAt = payload.exp * 1000; // 밀리초로 변환
  const now = Date.now();
  const bufferMs = bufferMinutes * 60 * 1000;
  
  return now + bufferMs > expiresAt;
};

// 토큰 갱신 재시도 플래그 (무한루프 방지)
let isRefreshing = false;
let failedQueue: { resolve: (v: any) => void; reject: (e: any) => void }[] = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((p) => (error ? p.reject(error) : p.resolve(token)));
  failedQueue = [];
};

// ── 요청 인터셉터 ──────────────────────────────────────────────────
apiClient.interceptors.request.use(
  async (config) => {
    const authState = useAuthStore.getState();
    const token = authState.accessToken;
    
    if (!token) {
      return config;
    }

    // 🔧 토큰이 5분 내에 만료되면 미리 갱신
    if (isTokenExpiringSoon(token)) {
      const refreshToken = authState.refreshToken;
      if (refreshToken && !isRefreshing) {
        isRefreshing = true;
        try {
          const { data } = await axios.post(
            `${apiClient.defaults.baseURL}/member/refresh`,
            { refreshToken },
            { headers: { 'Content-Type': 'application/json' } }
          );
          const newAccessToken: string = data.accessToken;
          const newRefreshToken: string = data.refreshToken;
          
          authState.setTokens(newAccessToken, newRefreshToken);
          config.headers.Authorization = `Bearer ${newAccessToken}`;
          
          console.log('[JWT] Token refreshed preemptively');
        } catch (error) {
          console.error('[JWT] Preemptive refresh failed:', error);
          authState.logout();
          window.location.href = '/auth';
        } finally {
          isRefreshing = false;
        }
      }
    } else {
      // 정상 토큰 주입
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => Promise.reject(error)
);

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
