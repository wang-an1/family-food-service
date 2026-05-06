package com.familyfood.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("scheduled_task_run_log")
public class ScheduledTaskRunLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long scheduledTaskId;
    private String triggerType;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long durationMillis;
    private String errorMessage;
    private LocalDateTime createdAt;
}
