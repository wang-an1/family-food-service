package com.familyfood.family.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "家庭")
public class Family {
    @TableId(type = IdType.AUTO)
    @Schema(description = "家庭 ID", example = "1")
    private Long id;
    @Schema(description = "家庭名称", example = "我的家庭")
    private String name;
    @TableField("invite_code")
    @Schema(description = "邀请码")
    private String inviteCode;
    @Schema(description = "家庭状态", example = "ACTIVE")
    private String status;
    @TableField("created_at")
    @Schema(description = "创建时间", format = "date-time")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
