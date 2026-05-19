import apiClient from './axios';
import type {
  Page,
  FanScheduleResponse,
  FanScheduleRequest,
  FanToggleResponse,
  FanControlRequest,
  FanControlResponse,
  FanAutoModeResponse,
  FanHistoryResponse,
  FanStatsResponse,
} from '../types/api';

/**
 * 환풍기(Fan) 관련 API
 * 백엔드: FanController (/devices/{houseId}/fan)
 */

/** 환풍기 스케줄 목록 조회 */
export const getFanSchedules = async (
  houseId: number | string,
  page = 0,
  size = 20,
): Promise<Page<FanScheduleResponse>> => {
  const { data } = await apiClient.get<Page<FanScheduleResponse>>(
    `/devices/${houseId}/fan/schedules`,
    { params: { page, size } },
  );
  return data;
};

/** 환풍기 스케줄 등록 */
export const createFanSchedule = async (
  houseId: number | string,
  request: FanScheduleRequest,
): Promise<FanScheduleResponse> => {
  const { data } = await apiClient.post<FanScheduleResponse>(
    `/devices/${houseId}/fan/schedules`,
    request,
  );
  return data;
};

/** 환풍기 스케줄 수정 */
export const updateFanSchedule = async (
  houseId: number | string,
  scheduleId: number,
  request: FanScheduleRequest,
): Promise<FanScheduleResponse> => {
  const { data } = await apiClient.put<FanScheduleResponse>(
    `/devices/${houseId}/fan/schedules/${scheduleId}`,
    request,
  );
  return data;
};

/** 환풍기 스케줄 활성/비활성 토글 */
export const toggleFanSchedule = async (
  houseId: number | string,
  scheduleId: number,
  enabled: boolean,
): Promise<FanToggleResponse> => {
  const { data } = await apiClient.patch<FanToggleResponse>(
    `/devices/${houseId}/fan/schedules/${scheduleId}/toggle`,
    null,
    { params: { enabled } },
  );
  return data;
};

/** 환풍기 스케줄 삭제 */
export const deleteFanSchedule = async (
  houseId: number | string,
  scheduleId: number,
): Promise<number> => {
  const { data } = await apiClient.delete<number>(
    `/devices/${houseId}/fan/schedules/${scheduleId}`,
  );
  return data;
};

/** 환풍기 수동 제어 */
export const controlFan = async (
  houseId: number | string,
  request: FanControlRequest,
): Promise<FanControlResponse> => {
  const { data } = await apiClient.post<FanControlResponse>(
    `/devices/${houseId}/fan/control`,
    request,
  );
  return data;
};

/** 환풍기 전체 자동 모드 설정 */
export const toggleFanAutoMode = async (
  houseId: number | string,
  isAutoMode: boolean,
): Promise<FanAutoModeResponse> => {
  const { data } = await apiClient.patch<FanAutoModeResponse>(
    `/devices/${houseId}/fan/auto-mode`,
    null,
    { params: { isAutoMode } },
  );
  return data;
};

/** 환풍기 최근 작동 이력 조회 */
export const getFanHistory = async (
  houseId: number | string,
  page = 0,
  size = 10,
): Promise<Page<FanHistoryResponse>> => {
  const { data } = await apiClient.get<Page<FanHistoryResponse>>(
    `/devices/${houseId}/fan/history`,
    { params: { page, size } },
  );
  return data;
};

/** 환풍기 오늘 작동 통계 조회 */
export const getFanStatistics = async (
  houseId: number | string,
): Promise<FanStatsResponse> => {
  const { data } = await apiClient.get<FanStatsResponse>(
    `/devices/${houseId}/fan/statistics`,
  );
  return data;
};
