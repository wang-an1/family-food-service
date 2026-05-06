package com.familyfood.auth.controller;

import com.familyfood.auth.dto.LoginRequest;
import com.familyfood.auth.dto.LoginResponse;
import com.familyfood.auth.dto.MeResponse;
import com.familyfood.auth.service.AuthService;
import com.familyfood.common.ApiResponse;
import com.familyfood.common.context.ActorContextProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证", description = "登录、当前用户和退出登录")
public class AuthController {
    private final AuthService authService;
    private final ActorContextProvider actorProvider;

    public AuthController(AuthService authService, ActorContextProvider actorProvider) {
        this.authService = authService;
        this.actorProvider = actorProvider;
    }

    @PostMapping("/login")
    @Operation(summary = "登录", description = "使用用户名和密码换取 JWT 访问令牌")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(new LoginRequest(request.username(), request.password())));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户", description = "根据 JWT 返回当前登录用户信息")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<MeResponse> me() {
        return ApiResponse.ok(authService.me(actorProvider.current()));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "前端清理 JWT 后调用，用于统一退出流程")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok();
    }
}
