package com.familyfood.file.controller;

import com.familyfood.common.ApiResponse;
import com.familyfood.file.dto.UploadResponse;
import com.familyfood.file.service.FileService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "业务文件上传接口")
@SecurityRequirement(name = "bearerAuth")
public class FileController {
    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件", description = "上传图片或附件，并返回可访问 URL")
    public ApiResponse<UploadResponse> upload(
            @Parameter(description = "上传文件", required = true, schema = @Schema(type = "string", format = "binary")) @RequestParam MultipartFile file,
            @Parameter(description = "业务类型", required = true, example = "dish") @NotBlank @RequestParam String bizType) throws IOException {
        return ApiResponse.ok(fileService.upload(file, bizType));
    }
}
