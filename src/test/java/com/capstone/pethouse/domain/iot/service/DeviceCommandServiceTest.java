package com.capstone.pethouse.domain.iot.service;

import com.capstone.pethouse.domain.iot.dto.CommandFetchResponse;
import com.capstone.pethouse.domain.iot.entity.DeviceCommand;
import com.capstone.pethouse.domain.iot.enums.CommandStatus;
import com.capstone.pethouse.domain.iot.repository.DeviceCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DeviceCommandServiceTest {

    @InjectMocks
    private DeviceCommandService deviceCommandService;

    @Mock
    private DeviceCommandRepository deviceCommandRepository;

    private DeviceCommand cmd(Long seq, String sn, String ct, CommandStatus status) {
        DeviceCommand c = DeviceCommand.enqueue(sn, ct);
        ReflectionTestUtils.setField(c, "seq", seq);
        ReflectionTestUtils.setField(c, "status", status);
        return c;
    }

    @Test
    @DisplayName("enqueue - 명령을 W 상태로 큐에 추가")
    void enqueueSuccess() {
        DeviceCommand saved = cmd(1L, "SN-001", "FEED", CommandStatus.W);
        given(deviceCommandRepository.save(any(DeviceCommand.class))).willReturn(saved);

        DeviceCommand result = deviceCommandService.enqueue("SN-001", "FEED");

        assertThat(result.getSn()).isEqualTo("SN-001");
        assertThat(result.getCt()).isEqualTo("FEED");
        assertThat(result.getStatus()).isEqualTo(CommandStatus.W);
    }

    @Test
    @DisplayName("enqueue 실패 - SN/CT 누락")
    void enqueueFail() {
        assertThatThrownBy(() -> deviceCommandService.enqueue(null, "FEED"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> deviceCommandService.enqueue("SN-001", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> deviceCommandService.enqueue("SN-001", "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("fetch - W 명령 반환 + status W→S 전환")
    void fetchSuccess() {
        DeviceCommand c1 = cmd(1L, "SN-001", "FEED", CommandStatus.W);
        DeviceCommand c2 = cmd(2L, "SN-001", "WATER", CommandStatus.W);

        given(deviceCommandRepository.findForFetchWithLock("SN-001", CommandStatus.W))
                .willReturn(List.of(c1, c2));

        List<CommandFetchResponse> result = deviceCommandService.fetch("SN-001");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).seq()).isEqualTo(1L);
        assertThat(result.get(0).ct()).isEqualTo("FEED");
        assertThat(result.get(1).ct()).isEqualTo("WATER");

        // status 전환 확인
        assertThat(c1.getStatus()).isEqualTo(CommandStatus.S);
        assertThat(c2.getStatus()).isEqualTo(CommandStatus.S);
    }

    @Test
    @DisplayName("fetch - W 명령 없으면 빈 리스트")
    void fetchEmpty() {
        given(deviceCommandRepository.findForFetchWithLock("SN-002", CommandStatus.W))
                .willReturn(List.of());

        assertThat(deviceCommandService.fetch("SN-002")).isEmpty();
    }

    @Test
    @DisplayName("fetch 실패 - SN 누락")
    void fetchFailNoSn() {
        assertThatThrownBy(() -> deviceCommandService.fetch(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> deviceCommandService.fetch("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
