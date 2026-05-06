package com.familyfood.system.controller;

import com.familyfood.common.ApiResponse;
import com.familyfood.system.dto.ScheduledTaskPreviewRequest;
import com.familyfood.system.dto.ScheduledTaskPreviewResponse;
import com.familyfood.system.dto.ScheduledTaskRequest;
import com.familyfood.system.dto.ScheduledTaskResponse;
import com.familyfood.system.dto.ScheduledTaskRunLogResponse;
import com.familyfood.system.dto.ScheduledTaskTypeResponse;
import com.familyfood.system.service.ScheduledTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
@Tag(name = "Scheduled tasks", description = "Global scheduled task management")
@SecurityRequirement(name = "bearerAuth")
public class ScheduledTaskController {
    private final ScheduledTaskService service;

    public ScheduledTaskController(ScheduledTaskService service) {
        this.service = service;
    }

    @GetMapping("/scheduled-task-types")
    @Operation(summary = "List scheduled task types")
    public ApiResponse<List<ScheduledTaskTypeResponse>> types() {
        return ApiResponse.ok(service.types());
    }

    @PostMapping("/scheduled-tasks/preview")
    @Operation(summary = "Preview schedule fire times")
    public ApiResponse<ScheduledTaskPreviewResponse> preview(@Valid @RequestBody ScheduledTaskPreviewRequest request) {
        return ApiResponse.ok(service.preview(request));
    }

    @GetMapping("/scheduled-tasks")
    @Operation(summary = "List scheduled tasks")
    public ApiResponse<List<ScheduledTaskResponse>> list(@RequestParam(required = false) String status,
                                                         @RequestParam(required = false) String taskType) {
        return ApiResponse.ok(service.list(status, taskType));
    }

    @PostMapping("/scheduled-tasks")
    @Operation(summary = "Create scheduled task")
    public ApiResponse<ScheduledTaskResponse> create(@Valid @RequestBody ScheduledTaskRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PutMapping("/scheduled-tasks/{id}")
    @Operation(summary = "Update scheduled task")
    public ApiResponse<ScheduledTaskResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody ScheduledTaskRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @PostMapping("/scheduled-tasks/{id}/enable")
    @Operation(summary = "Enable scheduled task")
    public ApiResponse<ScheduledTaskResponse> enable(@PathVariable Long id) {
        return ApiResponse.ok(service.enable(id));
    }

    @PostMapping("/scheduled-tasks/{id}/disable")
    @Operation(summary = "Disable scheduled task")
    public ApiResponse<ScheduledTaskResponse> disable(@PathVariable Long id) {
        return ApiResponse.ok(service.disable(id));
    }

    @PostMapping("/scheduled-tasks/{id}/trigger")
    @Operation(summary = "Trigger scheduled task once")
    public ApiResponse<ScheduledTaskResponse> trigger(@PathVariable Long id) {
        return ApiResponse.ok(service.triggerNow(id));
    }

    @DeleteMapping("/scheduled-tasks/{id}")
    @Operation(summary = "Delete scheduled task")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/scheduled-tasks/{id}/logs")
    @Operation(summary = "List scheduled task run logs")
    public ApiResponse<List<ScheduledTaskRunLogResponse>> logs(@PathVariable Long id) {
        return ApiResponse.ok(service.logs(id));
    }
}
