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

export interface RegisterPayload {
  memberId: string;
  memberPw: string;
  memberName: string;
  memberPhone: string;
  roleCode?: string;
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

/** 회원가입 (일반 유저) */
export const register = async (payload: RegisterPayload): Promise<any> => {
  const { data } = await apiClient.post('/member/register', {
    member_id: payload.memberId,
    member_pw: payload.memberPw,
    member_name: payload.memberName,
    member_phone: payload.memberPhone,
    role_code: payload.roleCode ?? 'USER',
  });
  return data;
};

/** 토큰 갱신 */
export const refreshToken = async (token: string): Promise<TokenRefreshResponse> => {
  const { data } = await apiClient.post<TokenRefreshResponse>('/member/refresh', {
    refreshToken: token,
  });
  return data;
};
