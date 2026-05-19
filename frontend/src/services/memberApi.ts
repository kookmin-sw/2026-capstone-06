import apiClient from './axios';

export interface MemberDto {
  seq: number;
  memberId: string;
  memberName: string;
  memberPhone: string;
  roleCode: "ADMIN" | "USER";
  roleName: string;
  regDate: string;
  enabled: boolean;
}

export interface MemberPageResponse {
  content: MemberDto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const getMembers = async (searchType?: string, searchQuery?: string): Promise<MemberPageResponse> => {
  const { data } = await apiClient.get<MemberPageResponse>('/member/list', {
    params: { searchType, searchQuery, size: 1000 }
  });
  return data;
};

export const checkId = async (memberId: string): Promise<boolean> => {
  const { data } = await apiClient.get<{ available: boolean }>('/member/checkId', {
    params: { memberId }
  });
  return data.available;
};

export const registerByAdmin = async (req: any): Promise<MemberDto> => {
  // MemberRegisterRequest
  const { data } = await apiClient.post<MemberDto>('/member/form', {
    member_id: req.memberId,
    member_pw: req.memberPw,
    member_name: req.memberName,
    member_phone: req.memberPhone,
    role_code: req.roleCode
  });
  return data;
};

export const updateByAdmin = async (req: any): Promise<MemberDto> => {
  // MemberModifyRequest
  const { data } = await apiClient.put<MemberDto>('/member/form', {
    seq: req.seq,
    member_id: req.memberId,
    member_pw: req.memberPw,
    member_name: req.memberName,
    member_phone: req.memberPhone,
    role_code: req.roleCode
  });
  return data;
};

export const deleteMember = async (seq: number, memberId: string): Promise<any> => {
  // MemberDeleteRequest
  const { data } = await apiClient.delete('/member', {
    data: { seq, member_id: memberId }
  });
  return data;
};
