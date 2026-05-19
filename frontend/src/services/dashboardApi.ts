import apiClient from './axios';
import type { SensorDataRes, DashboardInitRes, ActivityRes, DailyStatsRes } from '../types/api';

/**
 * 대시보드 관련 API
 * 백엔드: DashboardController (/dashboard)
 */

/** 최신 센서 데이터 조회 */
export const getLatestSensorData = async (deviceId: string): Promise<SensorDataRes> => {
  const { data } = await apiClient.get<SensorDataRes>('/dashboard/latest', {
    params: { deviceId },
  });
  return data;
};

/** 대시보드 초기 데이터 조회 (디바이스 목록 + 최신 센서) */
export const getDashboardInit = async (memberId: string): Promise<DashboardInitRes> => {
  const { data } = await apiClient.get<DashboardInitRes>('/dashboard/init', {
    params: { memberId },
  });
  return data;
};

/** 디바이스 목록 조회 */
export const getDevices = async (memberId: string): Promise<any[]> => {
  const { data } = await apiClient.get<any[]>('/dashboard/devices', {
    params: { memberId },
  });
  return data;
};

/** 대시보드 최근 활동 타임라인 조회 */
export const getDashboardActivities = async (deviceId: string): Promise<ActivityRes[]> => {
  const { data } = await apiClient.get<ActivityRes[]>('/dashboard/activities', {
    params: { deviceId },
  });
  return data;
};

/** 대시보드 당일 활동 요약 통계 조회 */
export const getDashboardStats = async (deviceId: string): Promise<DailyStatsRes> => {
  const { data } = await apiClient.get<DailyStatsRes>('/dashboard/stats', {
    params: { deviceId },
  });
  return data;
};
