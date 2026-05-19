/**
 * 백엔드 DTO에 대응하는 프론트엔드 타입 정의
 * backend/pethouse 폴더의 Java record 기반으로 작성
 */

// ── Spring Page 응답 공통 타입 ──────────────────────────────────────
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // 현재 페이지 (0-indexed)
  first: boolean;
  last: boolean;
  empty: boolean;
}

// ── Dashboard ───────────────────────────────────────────────────────

export interface SensorDataRes {
  deviceId: string;
  temperature: number | null;
  humidity: number | null;
  heartRate: number | null;
  co2: number | null;
  lastUpdate: string | null;
}

export interface DeviceRes {
  seq: number;
  deviceId: string;
  memberId: string;
  serialNum: string;
  deviceType: string;
  isUse: boolean;
  regDate: string;
}

export interface CodeRes {
  code: string;
  codeName: string;
  groupCode: string;
}

export interface DashboardInitRes {
  devices: DeviceRes[];
  selectedDeviceId: string;
  latestData: SensorDataRes;
}

// ── Hospital ────────────────────────────────────────────────────────

export interface HospitalListResponse {
  seq: number;
  name: string;
  location: string;
  phone: string;
  latitude: number;
  longitude: number;
  mainMedCode: string;
  regDate: string;
}

export interface MedCodeDto {
  hospitalSeq: number;
  medCode: string;
}

export interface HospitalDetailResponse {
  medList: MedCodeDto[];
  hospital: HospitalListResponse;
}

// ── Fan (환풍기) ────────────────────────────────────────────────────

export interface FanScheduleDetailResponse {
  temperature: number;
  speed: number;
}

export interface FanScheduleResponse {
  houseId: number;
  scheduleId: number;
  fanScheduleDetailResponseList: FanScheduleDetailResponse[];
  startTime: string;  // "HH:mm:ss" 형식
  endTime: string;
}

export interface FanToggleResponse {
  houseId: number;
  scheduleId: number;
  enabled: boolean;
}

export interface FanScheduleDetailRequest {
  temperature: number;
  speed: number;
}

export interface FanScheduleRequest {
  fanScheduleDetailRequestList: FanScheduleDetailRequest[];
  startTime: string;
  endTime: string;
  enabled: boolean;
}

export interface FanControlRequest {
  isRunning: boolean;
  intensity: number;
}

export interface FanControlResponse {
  houseId: number;
  isRunning: boolean;
  intensity: number;
}

export interface FanAutoModeResponse {
  houseId: number;
  isAutoMode: boolean;
}

export interface FanHistoryResponse {
  id: number;
  startTime: string;
  endTime: string;
  durationMinutes: number;
  speed: number;
  triggerType: 'MANUAL' | 'AUTO';
  executionStatus: 'SUCCESS' | 'FAIL';
  createdAt: string;
}

export interface FanStatsResponse {
  dailyCount: number;
  dailyOperatingHours: number;
  averageIntensity: number;
  autoModeRatio: number;
}

// ── Supply (급여/급수) ──────────────────────────────────────────────

/** 백엔드 enum: FOOD, WATER */
export type FeedType = 'FOOD' | 'WATER';

/** 백엔드 enum: GRAM, ML */
export type UnitType = 'GRAM' | 'ML';

/** 백엔드 enum: AUTO, MANUAL */
export type TriggerType = 'AUTO' | 'MANUAL';

/** 백엔드 enum: SUCCESS, FAIL */
export type ExecutionStatus = 'SUCCESS' | 'FAIL';

export interface SupplyScheduleResponse {
  houseId: number;
  scheduleId: number;
  feedType: FeedType;
  unitType: UnitType;
  amount: number;
  cronExpression: string;
  enabled: boolean;
  lastRunAt: string | null;
}

export interface SupplyLogHistoryResponse {
  houseId: number;
  scheduleId: number | null;
  feedType: FeedType;
  unitType: UnitType;
  amount: number;
  triggerType: TriggerType;
  executionStatus: ExecutionStatus;
  createdAt: string;
}

export interface SupplyScheduleRequest {
  feedType: FeedType;
  unitType: UnitType;
  amount: number;
  cronExpression: string;
  enabled: boolean;
}

export interface SupplyLogRequest {
  scheduleId: number | null;
  feedType: FeedType;
  unitType: UnitType;
  amount: number;
  triggerType: TriggerType;
}

export interface SupplyLogResponse {
  houseId: number;
  feedType: FeedType;
  unitType: UnitType;
  amount: number;
  triggerType: TriggerType;
  executionStatus: ExecutionStatus;
}

export interface SupplyToggleResponse {
  houseId: number;
  scheduleId: number;
  enabled: boolean;
}

export interface SensorResponse {
  seq: number;
  deviceId: string;
  temVal: number | null;
  humVal: number | null;
  coVal: number | null;
  regDate: string;
}
