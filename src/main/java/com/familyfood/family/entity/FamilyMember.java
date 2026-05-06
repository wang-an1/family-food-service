package com.familyfood.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@TableName("family_member")
@Schema(description = "家庭成员")
public class FamilyMember {
    @TableId(type = IdType.AUTO)
    @Schema(description = "家庭成员 ID", example = "1")
    private Long id;
    @TableField("family_id")
    @Schema(description = "家庭 ID", example = "1")
    private Long familyId;
    @TableField("user_id")
    @Schema(description = "用户 ID", example = "2")
    private Long userId;
    @Schema(description = "家庭角色", allowableValues = {"ADMIN", "MEMBER"}, example = "MEMBER")
    private String role;
    @TableField("display_name")
    @Schema(description = "家庭内显示名称", example = "爸爸")
    private String displayName;
    @Schema(description = "成员状态", allowableValues = {"ACTIVE", "DISABLED"}, example = "ACTIVE")
    private String status;
    @TableField("joined_at")
    @Schema(description = "加入时间", format = "date-time")
    private LocalDateTime joinedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
