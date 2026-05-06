package com.familyfood.system.scheduler;

import com.familyfood.system.service.ScheduledTaskService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class DelegatingScheduledTaskJob implements Job {
    private final ScheduledTaskService scheduledTaskService;

    public DelegatingScheduledTaskJob(ScheduledTaskService scheduledTaskService) {
        this.scheduledTaskService = scheduledTaskService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long scheduledTaskId = context.getMergedJobDataMap().getLong("scheduledTaskId");
        boolean manual = context.getMergedJobDataMap().getBoolean("manual");
        scheduledTaskService.executeScheduledTask(scheduledTaskId, manual);
    }
}
