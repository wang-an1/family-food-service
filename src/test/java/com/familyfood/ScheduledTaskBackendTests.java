package com.familyfood;

import com.familyfood.auth.dao.UserMapper;
import com.familyfood.auth.entity.User;
import com.familyfood.family.dao.FamilyMemberMapper;
import com.familyfood.family.entity.FamilyMember;
import com.familyfood.system.dto.ScheduledTaskParameterDefinition;
import com.familyfood.system.scheduler.ScheduledTaskExecutionContext;
import com.familyfood.system.scheduler.ScheduledTaskHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScheduledTaskBackendTests {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    AtomicInteger scheduledTaskExecutionCount;
    @Autowired
    UserMapper userMapper;
    @Autowired
    FamilyMemberMapper memberMapper;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void adminCanManageScheduledTaskFramework() throws Exception {
        String admin = bearer("admin", "admin123");
        mockMvc.perform(get("/api/system/scheduled-task-types").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].type", hasItem("TEST_NOOP")));

        mockMvc.perform(post("/api/system/scheduled-tasks/preview").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "scheduleMode", "DAILY",
                                "scheduleConfig", Map.of("timeOfDay", "18:30", "timeZone", "Asia/Shanghai")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cronExpression", is("0 30 18 * * ?")))
                .andExpect(jsonPath("$.data.nextFireTimes.length()", greaterThanOrEqualTo(1)));

        String createBody = mockMvc.perform(post("/api/system/scheduled-tasks").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "test schedule " + System.nanoTime(),
                                "taskType", "TEST_NOOP",
                                "status", "DISABLED",
                                "scheduleMode", "INTERVAL",
                                "scheduleConfig", Map.of("interval", 30, "intervalUnit", "MINUTES", "timeZone", "Asia/Shanghai"),
                                "parameters", Map.of("message", "hello")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("DISABLED")))
                .andExpect(jsonPath("$.data.runtimeState", is("PAUSED")))
                .andReturn().getResponse().getContentAsString();
        long taskId = objectMapper.readTree(createBody).path("data").path("id").asLong();

        mockMvc.perform(post("/api/system/scheduled-tasks/" + taskId + "/enable").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("ENABLED")));

        mockMvc.perform(post("/api/system/scheduled-tasks/" + taskId + "/disable").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("DISABLED")))
                .andExpect(jsonPath("$.data.runtimeState", is("PAUSED")));

        mockMvc.perform(put("/api/system/scheduled-tasks/" + taskId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "updated schedule " + System.nanoTime(),
                                "taskType", "TEST_NOOP",
                                "status", "DISABLED",
                                "scheduleMode", "WEEKLY",
                                "scheduleConfig", Map.of(
                                        "daysOfWeek", List.of(1, 3, 5),
                                        "timeOfDay", "07:15",
                                        "timeZone", "Asia/Shanghai"),
                                "parameters", Map.of("message", "updated")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scheduleMode", is("WEEKLY")))
                .andExpect(jsonPath("$.data.cronExpression", is("0 15 7 ? * MON,WED,FRI")));

        int before = scheduledTaskExecutionCount.get();
        mockMvc.perform(post("/api/system/scheduled-tasks/" + taskId + "/trigger").header("Authorization", admin))
                .andExpect(status().isOk());
        awaitSuccessfulRun(admin, taskId);
        assertTrue(scheduledTaskExecutionCount.get() > before);

        mockMvc.perform(delete("/api/system/scheduled-tasks/" + taskId).header("Authorization", admin))
                .andExpect(status().isOk());
    }

    @Test
    void scheduledTaskApisRequireGlobalAdmin() throws Exception {
        String member = bearer("member", "member123");
        mockMvc.perform(get("/api/system/scheduled-tasks").header("Authorization", member))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("FORBIDDEN")));

        String localAdminUsername = "local_admin_" + System.nanoTime();
        createAdminUser(localAdminUsername, "admin123");
        String localAdmin = bearer(localAdminUsername, "admin123");
        mockMvc.perform(get("/api/system/scheduled-tasks").header("Authorization", localAdmin))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("FORBIDDEN")));
    }

    @Test
    void rejectsUnavailableTaskTypeAndInvalidCron() throws Exception {
        String admin = bearer("admin", "admin123");
        mockMvc.perform(post("/api/system/scheduled-tasks").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "missing handler",
                                "taskType", "UNKNOWN_TYPE",
                                "status", "DISABLED",
                                "scheduleMode", "DAILY",
                                "scheduleConfig", Map.of("timeOfDay", "18:30")
                        ))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));

        mockMvc.perform(post("/api/system/scheduled-tasks/preview").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "scheduleMode", "CRON",
                                "cronExpression", "not a cron",
                                "scheduleConfig", Map.of("timeZone", "Asia/Shanghai")
                        ))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    private void awaitSuccessfulRun(String bearer, long taskId) throws Exception {
        for (int i = 0; i < 40; i++) {
            String body = mockMvc.perform(get("/api/system/scheduled-tasks/" + taskId + "/logs")
                            .header("Authorization", bearer))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            JsonNode logs = objectMapper.readTree(body).path("data");
            for (JsonNode log : logs) {
                if ("SUCCESS".equals(log.path("status").asText())) {
                    return;
                }
            }
            Thread.sleep(100);
        }
        mockMvc.perform(get("/api/system/scheduled-tasks/" + taskId + "/logs").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status", is("SUCCESS")));
    }

    private void createAdminUser(String username, String password) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname(username);
        user.setStatus("ACTIVE");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);

        FamilyMember member = new FamilyMember();
        member.setFamilyId(1L);
        member.setUserId(user.getId());
        member.setRole("ADMIN");
        member.setDisplayName(username);
        member.setStatus("ACTIVE");
        member.setJoinedAt(now);
        memberMapper.insert(member);
    }

    private String bearer(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(body).path("data").path("token").asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @TestConfiguration
    static class TestSchedulerConfig {
        @Bean
        AtomicInteger scheduledTaskExecutionCount() {
            return new AtomicInteger();
        }

        @Bean
        ScheduledTaskHandler testNoopScheduledTaskHandler(AtomicInteger scheduledTaskExecutionCount) {
            return new ScheduledTaskHandler() {
                @Override
                public String type() {
                    return "TEST_NOOP";
                }

                @Override
                public String name() {
                    return "Test no-op task";
                }

                @Override
                public String description() {
                    return "A test-only scheduled task handler";
                }

                @Override
                public List<ScheduledTaskParameterDefinition> parameterDefinitions() {
                    return List.of(new ScheduledTaskParameterDefinition(
                            "message", "Message", "STRING", false, "Test message", null));
                }

                @Override
                public void execute(ScheduledTaskExecutionContext context) {
                    scheduledTaskExecutionCount.incrementAndGet();
                }
            };
        }
    }
}
