import apiClient from './axios';
import type { SensorDataRes, DashboardInitRes } from '../types/api';

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
