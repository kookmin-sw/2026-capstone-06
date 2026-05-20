package com.capstone.pethouse.domain.file.controller;

import com.capstone.pethouse.domain.file.dto.FileResultResponse;
import com.capstone.pethouse.domain.file.dto.FileVo;
import com.capstone.pethouse.domain.file.entity.FileInfo;
import com.capstone.pethouse.domain.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/file")
@RestController
public class FileController {

    private final FileService fileService;

    @GetMapping("/list")
    public ResponseEntity<Page<FileVo>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "15") int pageSize,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String searchQuery) {
        return ResponseEntity.ok(fileService.getList(pageNum, pageSize, deviceId, searchQuery));
    }

    @GetMapping("/{seq}")
    public ResponseEntity<FileVo> get(@PathVariable Long seq,
                                       @RequestParam(required = false) String deviceId) {
        return ResponseEntity.ok(fileService.get(seq, deviceId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResultResponse> upload(
            @RequestParam String deviceId,
            @RequestParam(required = false) String filename,
            @RequestPart("file") MultipartFile file) {
        try {
            fileService.upload(deviceId, filename, file);
            return ResponseEntity.ok(FileResultResponse.ok("입력 완료"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(FileResultResponse.fail(e.getMessage()));
        }
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResultResponse> update(
            @RequestParam Long seq,
            @RequestParam String deviceId,
            @RequestParam(required = false) String filename,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            fileService.updateFile(seq, deviceId, filename, file);
            return ResponseEntity.ok(FileResultResponse.ok("파일이 수정되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(FileResultResponse.fail(e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<FileResultResponse> delete(@RequestBody Map<String, Object> body) {
        try {
            Long seq = Long.valueOf(body.get("seq").toString());
            String deviceId = body.get("deviceId") != null ? body.get("deviceId").toString() : null;
            fileService.delete(seq, deviceId);
            return ResponseEntity.ok(FileResultResponse.ok("삭제 완료"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(FileResultResponse.fail(e.getMessage()));
        }
    }

    @GetMapping("/audio/{seq}")
    public ResponseEntity<Resource> streamAudio(@PathVariable Long seq,
                                                 @RequestParam(required = false) String deviceId) {
        FileInfo info = fileService.getEntity(seq, deviceId);
        Resource resource = fileService.loadAsResource(seq, deviceId);

        String contentType = info.getContentType() != null ? info.getContentType() : "audio/mpeg";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + info.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/download/{seq}")
    public ResponseEntity<Resource> download(@PathVariable Long seq,
                                              @RequestParam(required = false) String deviceId) {
        FileInfo info = fileService.getEntity(seq, deviceId);
        Resource resource = fileService.loadAsResource(seq, deviceId);

        String encoded = URLEncoder.encode(info.getFilename(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"")
                .body(resource);
    }
}
