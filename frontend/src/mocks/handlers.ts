import { http, HttpResponse } from 'msw';
import { 
  MOCK_HOSPITALS,
  MOCK_MED_LIST,
  MOCK_FAN_SCHEDULES, 
  MOCK_SUPPLY_SCHEDULES, 
  MOCK_SUPPLY_HISTORY, 
  generateStatsData,
  getMockSensorData 
} from './db';

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';

export const handlers = [
  // Dashboard
  http.get(`${baseURL}/dashboard/latest`, ({ request }) => {
    const url = new URL(request.url);
    const deviceId = url.searchParams.get('deviceId') || 'default';
    return HttpResponse.json(getMockSensorData(deviceId));
  }),

  // Hospital
  http.get(`${baseURL}/hospital/list`, () => {
    return HttpResponse.json({
      content: MOCK_HOSPITALS,
      totalElements: MOCK_HOSPITALS.length,
      totalPages: 1,
      size: 100,
      number: 0,
    });
  }),

  http.get(`${baseURL}/hospital/:seq`, ({ params }) => {
    const { seq } = params;
    const hospital = MOCK_HOSPITALS.find(h => h.seq === Number(seq));
    if (!hospital) return new HttpResponse(null, { status: 404 });
    
    return HttpResponse.json({
      hospital,
      medList: MOCK_MED_LIST[Number(seq)] || [],
    });
  }),

  // Fan
  http.get(`${baseURL}/devices/:houseId/fan/schedules`, () => {
    return HttpResponse.json({
      content: MOCK_FAN_SCHEDULES,
      totalElements: MOCK_FAN_SCHEDULES.length,
    });
  }),

  // Supply
  http.get(`${baseURL}/devices/:houseId/supplier/schedules`, () => {
    return HttpResponse.json({
      content: MOCK_SUPPLY_SCHEDULES,
      totalElements: MOCK_SUPPLY_SCHEDULES.length,
    });
  }),

  http.get(`${baseURL}/devices/:houseId/supplier/record`, () => {
    return HttpResponse.json({
      content: MOCK_SUPPLY_HISTORY,
      totalElements: MOCK_SUPPLY_HISTORY.length,
    });
  }),

  http.post(`${baseURL}/devices/:houseId/supplier/record`, async ({ request }) => {
    const body = await request.json() as any;
    // 로컬 시간으로 저장 (Z 없음)
    const now = new Date();
    const localISOString = now.toISOString().replace('Z', '');
    const newRecord = {
      ...body,
      createdAt: localISOString,
      executionStatus: 'SUCCESS',
    };
    // Mock 이력에 추가
    MOCK_SUPPLY_HISTORY.unshift(newRecord);
    return HttpResponse.json(newRecord);
  }),



  // Statistics
  http.get(`${baseURL}/stats`, ({ request }) => {
    const url = new URL(request.url);
    const period = (url.searchParams.get('period') as any) || 'daily';
    return HttpResponse.json(generateStatsData(period));
  }),
];
