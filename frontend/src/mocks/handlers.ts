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



  // Statistics
  http.get(`${baseURL}/stats`, ({ request }) => {
    const url = new URL(request.url);
    const period = (url.searchParams.get('period') as any) || 'daily';
    return HttpResponse.json(generateStatsData(period));
  }),
];
