package com.familyfood.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("system_config")
@Schema(description = "系统配置")
public class SystemConfig {
    @TableId(type = IdType.AUTO)
    @Schema(description = "配置记录 ID", example = "1")
    private Long id;
    @Schema(description = "家庭 ID", example = "1")
    private Long familyId;
    @Schema(description = "配置键", example = "ai.provider")
    private String configKey;
    @Schema(description = "配置值", example = "deepseek")
    private String configValue;
    @Schema(description = "值类型", example = "STRING")
    private String valueType;
    @Schema(description = "是否加密存储，1 表示是", example = "0")
    private Integer encrypted;
    @Schema(description = "更新时间", format = "date-time")
    private LocalDateTime updatedAt;
}
