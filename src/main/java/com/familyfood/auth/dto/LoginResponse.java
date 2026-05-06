package com.familyfood.auth.dto;

import com.familyfood.auth.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录响应")
public record LoginResponse(
        @Schema(description = "JWT 访问令牌")
        String token,
        @Schema(description = "当前用户信息")
        MeResponse user) {
}
