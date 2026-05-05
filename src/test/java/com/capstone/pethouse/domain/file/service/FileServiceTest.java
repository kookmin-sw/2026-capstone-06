package com.capstone.pethouse.domain.file.service;

import com.capstone.pethouse.domain.file.entity.FileInfo;
import com.capstone.pethouse.domain.file.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @InjectMocks
    private FileService fileService;

    @Mock
    private FileRepository fileRepository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(fileService, "uploadDir", tempDir.toString());
    }

    private FileInfo createFile() {
        FileInfo f = FileInfo.of("DEV001", "test.mp3", tempDir.resolve("test.mp3").toString(), "audio/mpeg", 100L);
        ReflectionTestUtils.setField(f, "seq", 1L);
        ReflectionTestUtils.setField(f, "regDate", LocalDateTime.now());
        return f;
    }

    @Test
    @DisplayName("파일 업로드 성공 - 디스크 저장 + DB 등록")
    void uploadSuccess() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", "audio-content".getBytes());
        FileInfo saved = createFile();

        given(fileRepository.save(any(FileInfo.class))).willReturn(saved);

        FileInfo result = fileService.upload("DEV001", "test.mp3", file);

        assertThat(result.getDeviceId()).isEqualTo("DEV001");
        verify(fileRepository).save(any(FileInfo.class));
    }

    @Test
    @DisplayName("업로드 실패 - deviceId 누락")
    void uploadFailNoDeviceId() {
        MultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", "x".getBytes());

        assertThatThrownBy(() -> fileService.upload(null, "test.mp3", file))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("업로드 실패 - 빈 파일")
    void uploadFailEmptyFile() {
        MultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", new byte[0]);

        assertThatThrownBy(() -> fileService.upload("DEV001", "test.mp3", file))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("단일 조회 - deviceId 있을 때 findBySeqAndDeviceId 사용")
    void getWithDeviceId() {
        FileInfo f = createFile();
        given(fileRepository.findBySeqAndDeviceId(1L, "DEV001")).willReturn(Optional.of(f));

        var result = fileService.get(1L, "DEV001");

        assertThat(result.deviceId()).isEqualTo("DEV001");
    }

    @Test
    @DisplayName("단일 조회 - deviceId 없으면 findById")
    void getWithoutDeviceId() {
        FileInfo f = createFile();
        given(fileRepository.findById(1L)).willReturn(Optional.of(f));

        var result = fileService.get(1L, null);

        assertThat(result.seq()).isEqualTo(1L);
    }

    @Test
    @DisplayName("조회 실패")
    void getNotFound() {
        given(fileRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> fileService.get(999L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
