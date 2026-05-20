import apiClient from './axios';

export interface DeviceDto {
  seq: number;
  deviceId: string;
  memberId: string;
  serialNum: string;
  objectCode: string | null;
  objectName: string | null;
  deviceType: string;
  objectBirth: string | null;
  isUse: boolean;
  regDate: string;
}

export interface DevicePageResponse {
  content: DeviceDto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const getDevices = async (searchType?: string, searchQuery?: string): Promise<DevicePageResponse> => {
  const { data } = await apiClient.get<DevicePageResponse>('/device/list', {
    params: { searchType, searchQuery, size: 1000 }
  });
  return data;
};

export const createDevice = async (req: {
  deviceId: string;
  memberId: string;
  serialNum: string;
  deviceType: string;
  objectName?: string;
  objectBirth?: string;
  objectCode?: string;
  nickname?: string;
}): Promise<DeviceDto> => {
  const { data } = await apiClient.post<DeviceDto>('/device', {
    device_id: req.deviceId,
    member_id: req.memberId,
    serial_num: req.serialNum,
    device_type: req.deviceType,
    object_name: req.objectName,
    object_birth: req.objectBirth,
    object_code: req.objectCode,
    nickname: req.nickname,
  });
  return data;
};

export const updateDevice = async (req: {
  seq: number;
  deviceId: string;
  memberId: string;
  serialNum: string;
  oldSerialNum: string;
  deviceType: string;
  objectName?: string;
  objectBirth?: string;
  objectCode?: string;
}): Promise<DeviceDto> => {
  const { data } = await apiClient.put<DeviceDto>('/device', {
    seq: req.seq,
    device_id: req.deviceId,
    member_id: req.memberId,
    serial_num: req.serialNum,
    old_serial_num: req.oldSerialNum,
    device_type: req.deviceType,
    object_name: req.objectName,
    object_birth: req.objectBirth,
    object_code: req.objectCode,
  });
  return data;
};

export const deleteDevice = async (seq: number): Promise<void> => {
  await apiClient.delete(`/device/${seq}`);
};

export const checkSerial = async (serialNum: string): Promise<string> => {
  const { data } = await apiClient.get<{ status: string }>('/device/checkSerial', {
    params: { serialNum }
  });
  return data.status; // "ok" | "in_use" | "not_exist"
};
