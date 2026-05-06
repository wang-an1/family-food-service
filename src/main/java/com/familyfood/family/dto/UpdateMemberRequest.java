package com.familyfood.family.dto;

import com.familyfood.family.entity.Family;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "家庭成员更新请求")
public record UpdateMemberRequest(
        @Schema(description = "家庭角色", allowableValues = {"ADMIN", "MEMBER"}, example = "MEMBER")
        @Pattern(regexp = "ADMIN|MEMBER")
        String role,
        @Schema(description = "家庭内显示名称", example = "妈妈")
        @Size(max = 100)
        String displayName,
        @Schema(description = "成员状态", allowableValues = {"ACTIVE", "DISABLED"}, example = "ACTIVE")
        @Pattern(regexp = "ACTIVE|DISABLED")
        String status
) {
}
