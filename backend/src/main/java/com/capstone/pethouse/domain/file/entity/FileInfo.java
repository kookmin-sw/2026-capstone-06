package com.capstone.pethouse.domain.file.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "file_info", indexes = {
        @Index(name = "idx_file_info_device_id", columnList = "deviceId")
})
@Entity
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false, length = 500)
    private String filePath;

    private String contentType;

    private Long fileSize;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime regDate;

    private FileInfo(String deviceId, String filename, String filePath, String contentType, Long fileSize) {
        this.deviceId = deviceId;
        this.filename = filename;
        this.filePath = filePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    public static FileInfo of(String deviceId, String filename, String filePath, String contentType, Long fileSize) {
        return new FileInfo(deviceId, filename, filePath, contentType, fileSize);
    }

    public void update(String deviceId, String filename, String filePath, String contentType, Long fileSize) {
        if (deviceId != null) this.deviceId = deviceId;
        if (filename != null) this.filename = filename;
        if (filePath != null) this.filePath = filePath;
        if (contentType != null) this.contentType = contentType;
        if (fileSize != null) this.fileSize = fileSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileInfo that)) return false;
        return this.seq != null && Objects.equals(this.seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(seq);
    }
}
