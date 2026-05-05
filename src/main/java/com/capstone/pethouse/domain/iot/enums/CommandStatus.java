package com.capstone.pethouse.domain.iot.enums;

/**
 * 디바이스 명령 큐 상태.
 * - W: Waiting — 큐에 등록된 직후 (디바이스가 아직 받지 못함)
 * - S: Sent — fetch 호출로 디바이스에 전달됨
 * - E: Executed — 디바이스가 결과 보고 (FEN처럼 상태 보존이 필요한 경우)
 */
public enum CommandStatus {
    W, S, E
}
