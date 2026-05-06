package com.familyfood;

import com.familyfood.auth.entity.User;
import com.familyfood.order.dto.OrderResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
class FamilyFoodApplicationTests {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void a0_mobileLoginCorsPreflightAllowsMobileDevPort() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5174")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5174"));
    }

    @Test
    void a_loginSuccessAndPasswordError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "admin", "password", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.role", is("ADMIN")));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "admin", "password", "bad"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")));
    }

    @Test
    void b_memberCannotAccessAdminSettings() throws Exception {
        mockMvc.perform(get("/api/system-configs").header("Authorization", bearer("member", "member123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("FORBIDDEN")));
    }

    @Test
    void c_adminCanManageDishAndMemberSeesOnlyActive() throws Exception {
        String admin = bearer("admin", "admin123");
        String tagBody = mockMvc.perform(post("/api/dish-tags").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "quick-test-" + System.nanoTime(), "color", "#22c55e"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long tagId = objectMapper.readTree(tagBody).path("data").path("id").asLong();

        String dishJson = json(Map.of(
                "name", "蒜蓉西兰花",
                "categoryId", 1,
                "taste", "清淡",
                "mealTypes", List.of("LUNCH", "DINNER"),
                "difficulty", "EASY",
                "estimatedMinutes", 12,
                "status", "ACTIVE",
                "tagIds", List.of(tagId),
                "ingredients", List.of(Map.of("name", "西兰花", "amount", 1, "unit", "颗", "category", "蔬菜", "required", true))
        ));
        String body = mockMvc.perform(post("/api/dishes").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dishJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryName").exists())
                .andExpect(jsonPath("$.data.tags[0].id", is((int) tagId)))
                .andExpect(jsonPath("$.data.ingredients[0].name").exists())
                .andExpect(jsonPath("$.data.name", is("蒜蓉西兰花")))
                .andReturn().getResponse().getContentAsString();
        long dishId = objectMapper.readTree(body).path("data").path("id").asLong();

        mockMvc.perform(get("/api/dishes").header("Authorization", admin)
                        .param("tagId", String.valueOf(tagId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id==" + dishId + ")]").isNotEmpty());

        mockMvc.perform(put("/api/dishes/" + dishId + "/status").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "INACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("INACTIVE")));

        mockMvc.perform(get("/api/dishes").header("Authorization", bearer("member", "member123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.name=='蒜蓉西兰花')]").isEmpty());
    }

    @Test
    void d_orderConfirmAndShoppingListMergeIngredients() throws Exception {
        String member = bearer("member", "member123");
        String admin = bearer("admin", "admin123");

        long sessionId = objectMapper.readTree(mockMvc.perform(post("/api/meal-sessions").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "test dinner " + System.nanoTime(),
                                "mealType", "DINNER",
                                "status", "OPEN",
                                "confirmRequired", true
                        ))))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString())
                .path("data").path("id").asLong();

        String tomatoEggName = "test tomato egg " + System.nanoTime();
        String dishBody = mockMvc.perform(post("/api/dishes").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", tomatoEggName,
                                "categoryId", 1,
                                "taste", "light",
                                "mealTypes", List.of("LUNCH", "DINNER"),
                                "difficulty", "EASY",
                                "estimatedMinutes", 12,
                                "status", "ACTIVE",
                                "ingredients", List.of(
                                        Map.of("name", "番茄", "amount", 2, "unit", "个", "category", "蔬菜", "required", true),
                                        Map.of("name", "鸡蛋", "amount", 3, "unit", "个", "category", "蛋类", "required", true)
                                )
                        ))))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();
        long tomatoEggId = objectMapper.readTree(dishBody).path("data").path("id").asLong();

        String orderBody = json(Map.of(
                "mealSessionId", sessionId,
                "note", "少盐",
                "avoidances", "不要香菜",
                "items", List.of(
                        Map.of("dishId", tomatoEggId, "quantity", 1, "unit", "份", "note", "多番茄"),
                        Map.of("dishId", tomatoEggId, "quantity", 1, "unit", "份", "note", "再来一份")
                )
        ));
        String orderResponse = mockMvc.perform(post("/api/orders").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("PENDING_CONFIRM")))
                .andReturn().getResponse().getContentAsString();
        long orderId = objectMapper.readTree(orderResponse).path("data").path("id").asLong();

        mockMvc.perform(post("/api/orders/" + orderId + "/confirm").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("note", "member tries"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/orders/" + orderId + "/confirm").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("note", "已确认"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("CONFIRMED")));

        String listBody = mockMvc.perform(get("/api/orders").header("Authorization", admin)
                        .param("mealSessionId", String.valueOf(sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userNickname").exists())
                .andExpect(jsonPath("$.data[0].items.length()", greaterThanOrEqualTo(1)))
                .andReturn().getResponse().getContentAsString();
        assertEquals(orderId, objectMapper.readTree(listBody).path("data").get(0).path("id").asLong());

        String summaryBody = mockMvc.perform(get("/api/orders/summary").header("Authorization", admin)
                        .param("mealSessionId", String.valueOf(sessionId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode summary = objectMapper.readTree(summaryBody).path("data");
        assertTrue(summary.has(tomatoEggName));
        assertEquals(2.0, summary.path(tomatoEggName).asDouble(), 0.01);

        mockMvc.perform(post("/api/shopping-lists/" + sessionId + "/generate").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.name=='番茄')].amount").value(4.0))
                .andExpect(jsonPath("$.data.items[?(@.name=='鸡蛋')].amount").value(6.0));
    }

    @Test
    void e_aiRecommendationAndPlatformDetection() throws Exception {
        String member = bearer("member", "member123");
        mockMvc.perform(post("/api/ai/recommendations").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("prompt", "今晚想吃清淡一点，适合孩子", "mealType", "DINNER", "maxResults", 6))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendations.length()", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.data.recommendations[0].reason").exists());

        String body = mockMvc.perform(post("/api/ai/parse-link").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("url", "https://v.douyin.com/example/", "fallbackText", "空气炸锅鸡翅"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sourceType", is("DOUYIN")))
                .andReturn().getResponse().getContentAsString();
        long taskId = objectMapper.readTree(body).path("data").path("taskId").asLong();

        awaitTaskStatus(member, taskId, "REVIEW_REQUIRED");

        mockMvc.perform(post("/api/ai/parse-link").header("Authorization", member)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("url", "http://127.0.0.1/private", "fallbackText", "内网链接"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sourceType", is("WEB")));
    }

    @Test
    void f_fileUploadValidatesImageSignature() throws Exception {
        String admin = bearer("admin", "admin123");
        byte[] png = new byte[]{
                (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
                0x00, 0x00, 0x00, 0x0d
        };
        MockMultipartFile valid = new MockMultipartFile("file", "dish.png", "image/png", png);

        mockMvc.perform(multipart("/api/files/upload").file(valid)
                        .param("bizType", "DISH_IMAGE")
                        .header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url", startsWith("/uploads/dishes/")))
                .andExpect(jsonPath("$.data.originalName", is("dish.png")));

        MockMultipartFile invalid = new MockMultipartFile("file", "dish.png", "image/png", "not-an-image".getBytes());
        mockMvc.perform(multipart("/api/files/upload").file(invalid)
                        .param("bizType", "DISH_IMAGE")
                        .header("Authorization", admin))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    private String bearer(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(body).path("data").path("token").asText();
    }

    private void awaitTaskStatus(String bearer, long taskId, String expectedStatus) throws Exception {
        for (int i = 0; i < 60; i++) {
            String body = mockMvc.perform(get("/api/ai/tasks/" + taskId).header("Authorization", bearer))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            String actualStatus = objectMapper.readTree(body).path("data").path("task").path("status").asText();
            if (expectedStatus.equals(actualStatus)) {
                return;
            }
            Thread.sleep(100);
        }
        mockMvc.perform(get("/api/ai/tasks/" + taskId).header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.task.status", is(expectedStatus)));
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
