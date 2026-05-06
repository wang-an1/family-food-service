package com.familyfood.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@TableName("user")
@Schema(description = "用户")
public class User {
    @TableId(type = IdType.AUTO)
    @Schema(description = "用户 ID", example = "1")
    private Long id;
    @Schema(description = "用户名", example = "admin")
    private String username;
    @TableField("password_hash")
    @Schema(hidden = true)
    private String passwordHash;
    @Schema(description = "昵称", example = "管理员")
    private String nickname;
    @TableField("avatar_url")
    @Schema(description = "头像 URL")
    private String avatarUrl;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "用户状态", example = "ACTIVE")
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
