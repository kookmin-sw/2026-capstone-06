import apiClient from './axios';
import type { Page, HospitalListResponse, HospitalDetailResponse } from '../types/api';

/**
 * 동물병원 관련 API
 * 백엔드: HospitalController (/hospital)
 */

/** 병원 목록 조회 (페이지네이션) */
export const getHospitalList = async (params?: {
  searchType?: string;
  searchQuery?: string;
  page?: number;
  size?: number;
}): Promise<Page<HospitalListResponse>> => {
  const { data } = await apiClient.get<Page<HospitalListResponse>>('/hospital/list', {
    params: {
      page: params?.page ?? 0,
      size: params?.size ?? 100,   // 지도에 모두 표시하기 위해 충분히 큰 사이즈
      searchType: params?.searchType,
      searchQuery: params?.searchQuery,
    },
  });
  return data;
};

/** 병원 상세 조회 */
export const getHospitalDetail = async (seq: number): Promise<HospitalDetailResponse> => {
  const { data } = await apiClient.get<HospitalDetailResponse>(`/hospital/${seq}`);
  return data;
};
