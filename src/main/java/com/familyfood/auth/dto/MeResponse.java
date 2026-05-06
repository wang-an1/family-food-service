package com.familyfood.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "当前用户信息")
public record MeResponse(
        @Schema(description = "用户 ID", example = "1")
        Long id,
        @Schema(description = "用户名", example = "admin")
        String username,
        @Schema(description = "昵称", example = "管理员")
        String nickname,
        @Schema(description = "家庭 ID", example = "1")
        Long familyId,
        @Schema(description = "家庭角色", allowableValues = {"ADMIN", "MEMBER"}, example = "ADMIN")
        String role) {
}
