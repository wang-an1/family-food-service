package com.familyfood.system.scheduler;

import com.familyfood.common.AppException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTaskHandlerRegistry {
    private final Map<String, ScheduledTaskHandler> handlers;

    @Autowired
    public ScheduledTaskHandlerRegistry(List<ScheduledTaskHandler> discoveredHandlers) {
        Map<String, ScheduledTaskHandler> mapped = new LinkedHashMap<>();
        for (ScheduledTaskHandler handler : discoveredHandlers) {
            String type = normalize(handler.type());
            if (mapped.containsKey(type)) {
                throw AppException.conflict("定时任务处理器类型重复：" + type);
            }
            mapped.put(type, handler);
        }
        this.handlers = Map.copyOf(mapped);
    }

    public Collection<ScheduledTaskHandler> all() {
        return handlers.values();
    }

    public boolean has(String type) {
        return handlers.containsKey(normalize(type));
    }

    public ScheduledTaskHandler get(String type) {
        return handlers.get(normalize(type));
    }

    private String normalize(String type) {
        return type == null ? "" : type.trim().toUpperCase();
    }
}
