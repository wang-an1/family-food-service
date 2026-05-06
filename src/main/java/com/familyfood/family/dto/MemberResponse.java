package com.familyfood.family.dto;

import com.familyfood.family.entity.Family;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "家庭成员响应")
public record MemberResponse(
        @Schema(description = "家庭成员 ID", example = "1")
        Long id,
        @Schema(description = "用户 ID", example = "2")
        Long userId,
        @Schema(description = "用户名", example = "member")
        String username,
        @Schema(description = "用户昵称", example = "成员")
        String nickname,
        @Schema(description = "家庭角色", allowableValues = {"ADMIN", "MEMBER"}, example = "MEMBER")
        String role,
        @Schema(description = "家庭内显示名称", example = "爸爸")
        String displayName,
        @Schema(description = "成员状态", allowableValues = {"ACTIVE", "DISABLED"}, example = "ACTIVE")
        String status) {
}
