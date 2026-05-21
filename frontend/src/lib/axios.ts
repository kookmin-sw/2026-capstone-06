import axios from 'axios';

// 1. 인스턴스 생성
const getBaseURL = () => {
  if (import.meta.env.PROD) {
    return 'http://ec2-3-35-226-221.ap-northeast-2.compute.amazonaws.com:8081';
  }
  return 'http://localhost:8081';
};

const api = axios.create({
  baseURL: getBaseURL(),
  timeout: 5000, // 5초 넘으면 에러
  headers: {
    'Content-Type': 'application/json',
  },
});

// 2. 요청 인터셉터 (선택 사항: 요청 보내기 전 토큰 삽입 등)
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 3. 응답 인터셉터 (선택 사항: 에러 공통 처리)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 로그아웃 처리 등
    }
    return Promise.reject(error);
  }
);

export default api;