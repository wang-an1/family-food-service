package com.familyfood.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("scheduled_task")
public class ScheduledTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String taskType;
    private String status;
    private String scheduleMode;
    private String cronExpression;
    private String scheduleConfigJson;
    private String parametersJson;
    private String jobName;
    private String jobGroup;
    private String triggerName;
    private String triggerGroup;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
