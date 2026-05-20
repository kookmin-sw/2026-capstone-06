import apiClient from './axios';

export interface SerialDto {
  seq: number;
  serialNum: string;
  isUse: boolean;
  regDate: string;
}

export interface SerialPageResponse {
  content: SerialDto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const getSerials = async (searchQuery?: string): Promise<SerialPageResponse> => {
  const { data } = await apiClient.get<SerialPageResponse>('/serial/list', {
    params: { searchQuery, size: 1000 }
  });
  return data;
};

export const addSerial = async (serialNum: string, isUse: boolean): Promise<any> => {
  const { data } = await apiClient.post('/serial/add', { serialNum, isUse });
  return data;
};

export const updateSerial = async (serialNum: string, isUse: boolean): Promise<any> => {
  const { data } = await apiClient.put('/serial/update', { serialNum, isUse });
  return data;
};

export const deleteSerial = async (seq: number): Promise<any> => {
  const { data } = await apiClient.delete(`/serial/delete/${seq}`);
  return data;
};

export const toggleSerialUse = async (serialNum: string, isUse: boolean): Promise<any> => {
  if (isUse) {
    const { data } = await apiClient.put(`/serial/use/${serialNum}`);
    return data;
  } else {
    const { data } = await apiClient.put(`/serial/release/${serialNum}`);
    return data;
  }
};

export const generateSerials = async (count: number): Promise<SerialDto[]> => {
  const { data } = await apiClient.post<SerialDto[]>('/serial/generate', null, {
    params: { count }
  });
  return data;
};
