package com.capstone.pethouse.domain.file.service;

import com.capstone.pethouse.domain.file.dto.FileVo;
import com.capstone.pethouse.domain.file.entity.FileInfo;
import com.capstone.pethouse.domain.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileService {

    private final FileRepository fileRepository;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public Page<FileVo> getList(int pageNum, int pageSize, String deviceId, String searchQuery) {
        PageRequest pageRequest = PageRequest.of(Math.max(pageNum - 1, 0), pageSize);
        return fileRepository.findAllWithSearch(deviceId, searchQuery, pageRequest).map(FileVo::from);
    }

    @Transactional(readOnly = true)
    public FileVo get(Long seq, String deviceId) {
        FileInfo file = findOne(seq, deviceId);
        return FileVo.from(file);
    }

    @Transactional
    public FileInfo upload(String deviceId, String filename, MultipartFile multipartFile) throws IOException {
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId는 필수입니다.");
        }
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("file은 필수입니다.");
        }

        String storedFilename = UUID.randomUUID() + "_" + (filename != null ? filename : multipartFile.getOriginalFilename());
        Path destPath = ensureUploadDir().resolve(storedFilename);
        Files.copy(multipartFile.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);

        FileInfo info = FileInfo.of(
                deviceId,
                filename != null ? filename : multipartFile.getOriginalFilename(),
                destPath.toAbsolutePath().toString(),
                multipartFile.getContentType(),
                multipartFile.getSize()
        );
        return fileRepository.save(info);
    }

    @Transactional
    public FileInfo updateFile(Long seq, String deviceId, String filename, MultipartFile multipartFile) throws IOException {
        FileInfo file = findOne(seq, deviceId);

        String newPath = file.getFilePath();
        String newContentType = file.getContentType();
        Long newSize = file.getFileSize();

        if (multipartFile != null && !multipartFile.isEmpty()) {
            // 기존 파일 삭제
            try {
                Files.deleteIfExists(Paths.get(file.getFilePath()));
            } catch (IOException e) {
                log.warn("기존 파일 삭제 실패: {}", file.getFilePath());
            }

            String storedFilename = UUID.randomUUID() + "_" + (filename != null ? filename : multipartFile.getOriginalFilename());
            Path destPath = ensureUploadDir().resolve(storedFilename);
            Files.copy(multipartFile.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);

            newPath = destPath.toAbsolutePath().toString();
            newContentType = multipartFile.getContentType();
            newSize = multipartFile.getSize();
        }

        file.update(deviceId, filename, newPath, newContentType, newSize);
        return file;
    }

    @Transactional
    public void delete(Long seq, String deviceId) {
        FileInfo file = findOne(seq, deviceId);
        try {
            Files.deleteIfExists(Paths.get(file.getFilePath()));
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", file.getFilePath());
        }
        fileRepository.delete(file);
    }

    @Transactional(readOnly = true)
    public Resource loadAsResource(Long seq, String deviceId) {
        FileInfo file = findOne(seq, deviceId);
        try {
            Path path = Paths.get(file.getFilePath());
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("파일을 읽을 수 없습니다: " + file.getFilename());
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("파일 경로가 올바르지 않습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public FileInfo getEntity(Long seq, String deviceId) {
        return findOne(seq, deviceId);
    }

    private FileInfo findOne(Long seq, String deviceId) {
        if (deviceId != null && !deviceId.isBlank()) {
            return fileRepository.findBySeqAndDeviceId(seq, deviceId)
                    .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
        }
        return fileRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
    }

    private Path ensureUploadDir() throws IOException {
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }
}
