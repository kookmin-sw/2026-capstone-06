import type { 
  HospitalListResponse, 
  MedCodeDto, 
  FanScheduleResponse, 
  SupplyScheduleResponse, 
  SupplyLogHistoryResponse,
  SensorDataRes
} from '../types/api';

export const MOCK_HOSPITALS: HospitalListResponse[] = [
    { seq: 1, name: "행복동물병원", location: "경기도 성남시 분당구 정자일로 87", phone: "031-711-1234", latitude: 37.3630, longitude: 127.1050, mainMedCode: "med01", regDate: "2024-03-15" },
    { seq: 2, name: "BB동물병원", location: "경기도 성남시 분당구 동물사랑길 117", phone: "031-456-5123", latitude: 37.3596, longitude: 127.1054, mainMedCode: "med02", regDate: "2025-10-02" },
    { seq: 3, name: "사랑동물의원", location: "경기도 성남시 분당구 서현로 180번길 22", phone: "031-789-4567", latitude: 37.3520, longitude: 127.1100, mainMedCode: "med03", regDate: "2023-06-10" },
    { seq: 4, name: "분당24시동물병원", location: "경기도 성남시 분당구 야탑로 69", phone: "031-555-8888", latitude: 37.3480, longitude: 127.1270, mainMedCode: "med07", regDate: "2022-11-20" },
    { seq: 5, name: "판교동물의원", location: "경기도 성남시 분당구 판교역로 235", phone: "031-444-7777", latitude: 37.3980, longitude: 127.1080, mainMedCode: "med02", regDate: "2024-08-22" },
    { seq: 6, name: "정자동물병원", location: "경기도 성남시 분당구 정자동 119-1", phone: "031-222-3333", latitude: 37.3640, longitude: 127.1120, mainMedCode: "med06", regDate: "2023-12-01" },
    { seq: 7, name: "수내동물의원", location: "경기도 성남시 분당구 수내동 13-5", phone: "031-333-4444", latitude: 37.3570, longitude: 127.1200, mainMedCode: "med05", regDate: "2024-02-18" },
    { seq: 8, name: "야탑동물병원", location: "경기도 성남시 분당구 야탑동 370", phone: "031-666-9999", latitude: 37.3410, longitude: 127.1280, mainMedCode: "med03", regDate: "2024-07-30" },
    { seq: 9, name: "서현동물병원", location: "경기도 성남시 분당구 서현동 255-1", phone: "031-888-5555", latitude: 37.3501, longitude: 127.1170, mainMedCode: "med04", regDate: "2024-01-09" },
    { seq: 10, name: "구미동물의원", location: "경기도 성남시 분당구 구미동 11", phone: "031-777-2222", latitude: 37.3452, longitude: 127.1050, mainMedCode: "med01", regDate: "2023-09-18" },
];

export const MOCK_MED_LIST: Record<number, MedCodeDto[]> = {
    1: [{ hospitalSeq: 1, medCode: "med01" }, { hospitalSeq: 1, medCode: "med03" }, { hospitalSeq: 1, medCode: "med06" }],
    2: [{ hospitalSeq: 2, medCode: "med02" }, { hospitalSeq: 2, medCode: "med06" }],
    3: [{ hospitalSeq: 3, medCode: "med03" }, { hospitalSeq: 3, medCode: "med01" }],
    4: [{ hospitalSeq: 4, medCode: "med07" }, { hospitalSeq: 4, medCode: "med01" }, { hospitalSeq: 4, medCode: "med02" }],
    5: [{ hospitalSeq: 5, medCode: "med02" }, { hospitalSeq: 5, medCode: "med01" }],
    6: [{ hospitalSeq: 6, medCode: "med06" }, { hospitalSeq: 6, medCode: "med01" }, { hospitalSeq: 6, medCode: "med04" }],
    7: [{ hospitalSeq: 7, medCode: "med05" }],
    8: [{ hospitalSeq: 8, medCode: "med03" }, { hospitalSeq: 8, medCode: "med01" }],
    9: [{ hospitalSeq: 9, medCode: "med04" }, { hospitalSeq: 9, medCode: "med01" }],
    10: [{ hospitalSeq: 10, medCode: "med01" }, { hospitalSeq: 10, medCode: "med06" }],
};

export const MOCK_FAN_SCHEDULES: FanScheduleResponse[] = [
  {
    houseId: 1,
    scheduleId: 1,
    startTime: '08:00:00',
    endTime: '20:00:00',
    fanScheduleDetailResponseList: [
      { temperature: 22, speed: 50 },
      { temperature: 25, speed: 70 },
      { temperature: 28, speed: 90 },
    ],
  },
  {
    houseId: 1,
    scheduleId: 2,
    startTime: '20:00:00',
    endTime: '08:00:00',
    fanScheduleDetailResponseList: [
      { temperature: 24, speed: 40 },
      { temperature: 27, speed: 60 },
    ],
  },
];

export const MOCK_SUPPLY_SCHEDULES: SupplyScheduleResponse[] = [
  { houseId: 1, scheduleId: 1, feedType: 'FOOD', unitType: 'g', amount: 100, cronExpression: '0 0 8 * * ?', enabled: true, lastRunAt: null },
  { houseId: 1, scheduleId: 2, feedType: 'FOOD', unitType: 'g', amount: 100, cronExpression: '0 0 18 * * ?', enabled: true, lastRunAt: null },
  { houseId: 1, scheduleId: 3, feedType: 'WATER', unitType: 'ml', amount: 200, cronExpression: '0 0 9 * * ?', enabled: true, lastRunAt: null },
  { houseId: 1, scheduleId: 4, feedType: 'WATER', unitType: 'ml', amount: 200, cronExpression: '0 0 15 * * ?', enabled: true, lastRunAt: null },
  { houseId: 1, scheduleId: 5, feedType: 'WATER', unitType: 'ml', amount: 200, cronExpression: '0 0 21 * * ?', enabled: true, lastRunAt: null },
];

export const MOCK_SUPPLY_HISTORY: SupplyLogHistoryResponse[] = [
  { houseId: 1, scheduleId: null, feedType: 'FOOD', unitType: 'g', amount: 100, triggerType: 'MANUAL', executionStatus: 'SUCCESS', createdAt: '2026-05-21T13:30:00' },
  { houseId: 1, scheduleId: 1, feedType: 'FOOD', unitType: 'g', amount: 100, triggerType: 'AUTO', executionStatus: 'SUCCESS', createdAt: '2026-05-21T08:00:00' },
  { houseId: 1, scheduleId: 3, feedType: 'WATER', unitType: 'ml', amount: 200, triggerType: 'AUTO', executionStatus: 'SUCCESS', createdAt: '2026-05-20T15:00:00' },
  { houseId: 1, scheduleId: 1, feedType: 'FOOD', unitType: 'g', amount: 100, triggerType: 'AUTO', executionStatus: 'SUCCESS', createdAt: '2026-05-20T08:00:00' },
  { houseId: 1, scheduleId: 5, feedType: 'WATER', unitType: 'ml', amount: 200, triggerType: 'AUTO', executionStatus: 'SUCCESS', createdAt: '2026-05-19T21:00:00' },
];

export const MOCK_VENTILATION_HISTORY = [
  { id: '1', timestamp: '2026-05-21T13:30:00', duration: 10, intensity: 70, mode: 'auto', trigger: '온도 26°C 도달 (강도 70%)' },
  { id: '2', timestamp: '2026-05-21T11:30:00', duration: 15, intensity: 80, mode: 'manual' },
  { id: '5', timestamp: '2026-05-20T13:10:00', duration: 8, intensity: 50, mode: 'manual' },
];



export const generateStatsData = (period: 'daily' | 'weekly' | 'monthly') => {
  if (period === 'daily') {
    return Array.from({ length: 24 }, (_, i) => ({
      time: `${i}:00`,
      co2: 400 + Math.random() * 200,
      temperature: 20 + Math.random() * 5,
      humidity: 50 + Math.random() * 15,
      barking: Math.floor(Math.random() * 8),
    }));
  }
  if (period === 'weekly') {
    const days = ['월', '화', '수', '목', '금', '토', '일'];
    return days.map(day => ({
      time: day,
      co2: 400 + Math.random() * 150,
      temperature: 21 + Math.random() * 3,
      humidity: 52 + Math.random() * 10,
      barking: Math.floor(Math.random() * 30 + 5),
    }));
  }
  return Array.from({ length: 30 }, (_, i) => ({
    time: `${i + 1}일`,
    co2: 420 + Math.random() * 120,
    temperature: 21 + Math.random() * 3,
    humidity: 53 + Math.random() * 10,
    barking: Math.floor(Math.random() * 40 + 5),
  }));
};

export const getMockSensorData = (deviceId: string): SensorDataRes => ({
  deviceId,
  temperature: 20 + Math.random() * 5,
  humidity: 50 + Math.random() * 15,
  co2: 400 + Math.random() * 200,
  heartRate: 70 + Math.random() * 20,
  lastUpdate: new Date().toISOString(),
});
