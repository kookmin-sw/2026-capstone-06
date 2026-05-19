import apiClient from './axios';

export interface MemberSimpleResponse {
  seq: number;
  memberId: string;
  memberName: string;
  memberPhone: string;
  roleCode: string;
  roleName: string;
  regDate: string;
}

export interface MemberModifyRequest {
  seq: number;
  member_id: string;
  member_pw?: string;
  member_name: string;
  member_phone: string;
  role_code?: string;
}

/**
 * 회원 정보 조회
 */
export const getMemberByMemberId = async (memberId: string): Promise<MemberSimpleResponse> => {
  const { data } = await apiClient.get<MemberSimpleResponse>(`/member/id/${memberId}`);
  return data;
};

/**
 * 회원 정보 수정
 */
export const updateMember = async (payload: MemberModifyRequest): Promise<MemberSimpleResponse> => {
  const { data } = await apiClient.put<MemberSimpleResponse>('/member', payload);
  return data;
};

/**
 * 회원 탈퇴 신청 (계정 비활성화)
 */
export const deactivateMember = async (memberId: string, memberPw: string): Promise<MemberSimpleResponse> => {
  const { data } = await apiClient.patch<MemberSimpleResponse>('/member/deactivate', {
    memberId,
    memberPw,
  });
  return data;
};

/**
 * 회원 탈퇴 취소 (계정 재활성화)
 */
export const reactivateMember = async (memberId: string): Promise<MemberSimpleResponse> => {
  const { data } = await apiClient.patch<MemberSimpleResponse>('/member/reactivate', {
    memberId,
  });
  return data;
};

/**
 * 회원 계정 상태 조회 (enabled 여부)
 */
export const getAccountStatus = async (memberId: string): Promise<{ enabled: boolean }> => {
  const { data } = await apiClient.get<{ enabled: boolean }>('/member/status', {
    params: { memberId },
  });
  return data;
};
