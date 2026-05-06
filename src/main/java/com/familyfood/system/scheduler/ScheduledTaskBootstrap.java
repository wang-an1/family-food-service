package com.familyfood.system.scheduler;

import com.familyfood.system.service.ScheduledTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTaskBootstrap {
    private final ScheduledTaskService scheduledTaskService;

    @Autowired
    public ScheduledTaskBootstrap(ScheduledTaskService scheduledTaskService) {
        this.scheduledTaskService = scheduledTaskService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reconcileQuartzJobs() {
        scheduledTaskService.reconcileAll();
    }
}
