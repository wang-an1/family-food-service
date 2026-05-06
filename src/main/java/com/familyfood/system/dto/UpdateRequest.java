package com.familyfood.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;
import java.util.List;

@Schema(description = "系统配置批量更新请求")
public record UpdateRequest(
        @Schema(description = "配置项列表")
        @NotEmpty
        List<@Valid ConfigItem> configs
) {
}
