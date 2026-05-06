package com.familyfood.system.controller;

import com.familyfood.common.ApiResponse;
import com.familyfood.system.dto.ConfigResponse;
import com.familyfood.system.dto.UpdateRequest;
import com.familyfood.system.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system-configs")
@Tag(name = "系统配置", description = "家庭级系统配置读取和更新")
@SecurityRequirement(name = "bearerAuth")
public class SystemConfigController {
    private final SystemConfigService service;

    public SystemConfigController(SystemConfigService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "查询系统配置", description = "返回当前家庭可见的系统配置项")
    public ApiResponse<List<ConfigResponse>> list() {
        return ApiResponse.ok(service.list());
    }

    @PutMapping
    @Operation(summary = "更新系统配置", description = "批量更新当前家庭的系统配置项")
    public ApiResponse<List<ConfigResponse>> update(@Valid @RequestBody UpdateRequest request) {
        return ApiResponse.ok(service.update(request));
    }
}
