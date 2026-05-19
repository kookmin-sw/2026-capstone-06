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
  (error) => {
    return Promise.reject(error);
  }
);

// ── 응답 인터셉터 ──────────────────────────────────────────────────
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // 공통 에러 처리
    if (error.response) {
      const { status } = error.response;
      if (status === 401) {
        console.warn('[API] 인증 만료 - 로그아웃 후 로그인 페이지로 이동');
        useAuthStore.getState().logout();
        window.location.href = '/auth';
      } else if (status === 403) {
        console.warn('[API] 접근 권한 없음');
      } else if (status >= 500) {
        console.error('[API] 서버 오류:', error.response.data);
      }
    } else if (error.request) {
      console.error('[API] 서버 응답 없음 - 백엔드 미가동 또는 네트워크 오류');
    }
    return Promise.reject(error);
  }
);

export default apiClient;
