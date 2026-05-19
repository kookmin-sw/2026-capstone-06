import apiClient from './axios';

export interface LoginPayload {
  memberId: string;
  memberPw: string;
}

export interface LoginResponse {
  message: string;
  role: string;
  accessToken: string;
  refreshToken: string;
}

export interface TokenRefreshResponse {
  message: string;
  role: string;
  accessToken: string;
  refreshToken: string;
}

/**
 * 인증(Auth) 관련 API
 * 백엔드: AuthController (/member)
 */

/** 웹 로그인 */
export const loginWeb = async (payload: LoginPayload): Promise<LoginResponse> => {
  const { data } = await apiClient.post<LoginResponse>('/member/login', payload);
  return data;
};

/** 토큰 갱신 */
export const refreshToken = async (refreshToken: string): Promise<TokenRefreshResponse> => {
  const { data } = await apiClient.post<TokenRefreshResponse>('/member/refresh', {
    refreshToken,
  });
  return data;
};
