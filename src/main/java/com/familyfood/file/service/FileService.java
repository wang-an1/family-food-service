package com.familyfood.file.service;

import com.familyfood.file.dto.UploadResponse;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    UploadResponse upload(MultipartFile file, String bizType) throws IOException;
}
