import apiClient from './axios';
import type { SensorResponse } from '../types/api';

/**
 * 센서(Sensor) 관련 API
 * 백엔드: SensorController (/data)
 */

/** 차트 데이터 조회 (InfluxDB) */
export const getChartData = async (
  serialNum: string,
  range: string = '-24h',
  interval: string = '10m',
): Promise<SensorResponse[]> => {
  const { data } = await apiClient.get<SensorResponse[]>('/data/chart', {
    params: { serialNum, range, interval },
  });
  return data;
};
