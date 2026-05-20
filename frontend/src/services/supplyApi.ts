import apiClient from './axios';
import type {
  Page,
  SupplyScheduleResponse,
  SupplyScheduleRequest,
  SupplyToggleResponse,
  SupplyLogHistoryResponse,
  SupplyLogRequest,
  SupplyLogResponse,
} from '../types/api';

/**
 * 급여/급수(Supply) 관련 API
 * 백엔드: SupplyController (/devices/{houseId}/supplier)
 */

/** 급여/급수 스케줄 목록 조회 */
export const getSupplySchedules = async (
  houseId: number | string,
  page = 0,
  size = 20,
): Promise<Page<SupplyScheduleResponse>> => {
  const { data } = await apiClient.get<Page<SupplyScheduleResponse>>(
    `/devices/${houseId}/supplier/schedules`,
    { params: { page, size } },
  );
  return data;
};

/** 급여/급수 스케줄 등록 */
export const createSupplySchedule = async (
  houseId: number | string,
  request: SupplyScheduleRequest,
): Promise<SupplyScheduleResponse> => {
  const { data } = await apiClient.post<SupplyScheduleResponse>(
    `/devices/${houseId}/supplier/schedules`,
    request,
  );
  return data;
};

/** 급여/급수 스케줄 수정 */
export const updateSupplySchedule = async (
  houseId: number | string,
  scheduleId: number,
  request: SupplyScheduleRequest,
): Promise<SupplyScheduleResponse> => {
  const { data } = await apiClient.put<SupplyScheduleResponse>(
    `/devices/${houseId}/supplier/schedules/${scheduleId}`,
    request,
  );
  return data;
};

/** 급여/급수 스케줄 활성/비활성 토글 */
export const toggleSupplySchedule = async (
  houseId: number | string,
  scheduleId: number,
  enabled: boolean,
): Promise<SupplyToggleResponse> => {
  const { data } = await apiClient.patch<SupplyToggleResponse>(
    `/devices/${houseId}/supplier/schedules/${scheduleId}/toggle`,
    null,
    { params: { enabled } },
  );
  return data;
};

/** 급여/급수 스케줄 삭제 */
export const deleteSupplySchedule = async (
  houseId: number | string,
  scheduleId: number,
): Promise<number> => {
  const { data } = await apiClient.delete<number>(
    `/devices/${houseId}/supplier/schedules/${scheduleId}`,
  );
  return data;
};

/** 수동 급여/급수 기록 저장 */
export const recordSupplyLog = async (
  houseId: number | string,
  request: SupplyLogRequest,
): Promise<SupplyLogResponse> => {
  const { data } = await apiClient.post<SupplyLogResponse>(
    `/devices/${houseId}/supplier/record`,
    request,
  );
  return data;
};

/** 급여/급수 이력 조회 */
export const getSupplyHistory = async (
  houseId: number | string,
  page = 0,
  size = 20,
): Promise<Page<SupplyLogHistoryResponse>> => {
  const { data } = await apiClient.get<Page<SupplyLogHistoryResponse>>(
    `/devices/${houseId}/supplier/record`,
    { params: { page, size } },
  );
  return data;
};
