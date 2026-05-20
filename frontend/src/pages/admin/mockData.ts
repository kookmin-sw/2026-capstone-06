export interface Member {
  memberId: string;
  memberName: string;
  memberPhone: string;
  role: "USER" | "ADMIN";
  regDate: string;
}

export interface Serial {
  seq: number;
  serialNum: string;
  isUse: boolean;
  regDate: string;
}

export interface Device {
  seq: number;
  deviceId: string;
  memberId: string;
  serialNum: string;
  objectCode: string;
  objectName: string | null;
  deviceType: "HOUSE" | "COLLAR";
  objectBirth: string;
  isUse: boolean;
  regDate: string;
}

export const initialMembers: Member[] = [
  { memberId: "user01", memberName: "초코집사", memberPhone: "010-1111-2222", role: "USER", regDate: "2025-08-26 18:29:42" },
  { memberId: "user02", memberName: "나비집사", memberPhone: "010-3333-4444", role: "USER", regDate: "2025-09-01 13:59:52" },
  { memberId: "admin", memberName: "관리자", memberPhone: "010-9999-0000", role: "ADMIN", regDate: "2025-07-01 09:00:00" },
];

export const initialSerials: Serial[] = [
  { seq: 7, serialNum: "20251013-DEV-002", isUse: true, regDate: "2025-09-01 13:59:52" },
  { seq: 4, serialNum: "20251013-DEV-001", isUse: true, regDate: "2025-08-26 18:29:42" },
  { seq: 12, serialNum: "20251013-DEV-003", isUse: false, regDate: "2025-10-13 10:15:00" },
];

export const initialDevices: Device[] = [
  { seq: 1, deviceId: "DEV001", memberId: "user01", serialNum: "20251013-DEV-001", objectCode: "DOG001", objectName: "초코", deviceType: "HOUSE", objectBirth: "2020-01-01", isUse: true, regDate: "2025-11-21 15:36:40" },
  { seq: 2, deviceId: "DEV002", memberId: "user02", serialNum: "20251013-DEV-002", objectCode: "CAT001", objectName: "나비", deviceType: "COLLAR", objectBirth: "2019-06-01", isUse: true, regDate: "2025-11-06 12:10:00" },
];
