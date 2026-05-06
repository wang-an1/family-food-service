package com.familyfood.file.service;

import com.familyfood.common.AppException;
import com.familyfood.config.AppProperties;
import com.familyfood.file.dto.UploadResponse;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "webp");

    private final AppProperties properties;

    public FileService(AppProperties properties) {
        this.properties = properties;
    }

    public UploadResponse upload(MultipartFile file, String bizType) throws IOException {
        if (file.isEmpty()) {
            log.info("file_upload_rejected reason=empty bizType={} originalFilename={}", bizType, file.getOriginalFilename());
            throw AppException.validation("文件不能为空");
        }
        String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String ext = extension(original);
        if (!ALLOWED.contains(ext)) {
            log.info("file_upload_rejected reason=extension bizType={} originalFilename={} extension={}", bizType, original, ext);
            throw AppException.validation("仅支持 jpg、jpeg、png、webp 图片");
        }
        if (!hasAllowedSignature(ext, file)) {
            log.info("file_upload_rejected reason=signature bizType={} originalFilename={} extension={}", bizType, original, ext);
            throw AppException.validation("文件内容不是受支持的图片格式");
        }
        String dir = switch (bizType) {
            case "DISH_IMAGE" -> "dishes";
            case "AVATAR" -> "avatars";
            default -> "intents";
        };
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Path root = Path.of(properties.uploadDir()).toAbsolutePath().normalize();
        Path targetDir = root.resolve(dir).resolve(date).normalize();
        if (!targetDir.startsWith(root)) {
            log.warn("file_upload_rejected reason=target_dir_escape bizType={} targetDir={}", bizType, targetDir);
            throw AppException.validation("上传目录非法");
        }
        Files.createDirectories(targetDir);
        String filename = UUID.randomUUID() + "." + ext;
        Path target = targetDir.resolve(filename).normalize();
        if (!target.startsWith(targetDir)) {
            log.warn("file_upload_rejected reason=target_escape bizType={} target={}", bizType, target);
            throw AppException.validation("上传目录非法");
        }
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target);
        } catch (IOException ex) {
            log.error("file_upload_write_failed bizType={} target={} size={}", bizType, target, file.getSize(), ex);
            throw ex;
        }
        String url = "/uploads/" + dir + "/" + date + "/" + filename;
        log.info("file_upload_success bizType={} extension={} size={} url={}", bizType, ext, file.getSize(), url);
        return new UploadResponse(url, original, file.getSize());
    }

    private String extension(String name) {
        int index = name.lastIndexOf('.');
        if (index < 0 || index == name.length() - 1) {
            return "";
        }
        return name.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private boolean hasAllowedSignature(String ext, MultipartFile file) throws IOException {
        byte[] header;
        try (InputStream input = file.getInputStream()) {
            header = input.readNBytes(16);
        }
        return switch (ext) {
            case "jpg", "jpeg" -> startsWith(header, 0xff, 0xd8, 0xff);
            case "png" -> startsWith(header, 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a);
            case "webp" -> startsWith(header, 0x52, 0x49, 0x46, 0x46)
                    && startsWithAt(header, 8, 0x57, 0x45, 0x42, 0x50);
            default -> false;
        };
    }

    private boolean startsWith(byte[] actual, int... expected) {
        return startsWithAt(actual, 0, expected);
    }

    private boolean startsWithAt(byte[] actual, int offset, int... expected) {
        if (actual.length < offset + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((actual[offset + i] & 0xff) != expected[i]) {
                return false;
            }
        }
        return true;
    }
}
