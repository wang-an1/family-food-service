package com.familyfood.family.controller;

import com.familyfood.common.ApiResponse;
import com.familyfood.family.dto.MemberResponse;
import com.familyfood.family.dto.UpdateMemberRequest;
import com.familyfood.family.entity.Family;
import com.familyfood.family.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family")
@Tag(name = "家庭管理", description = "当前家庭和家庭成员管理")
@SecurityRequirement(name = "bearerAuth")
public class FamilyController {
    private final FamilyService service;

    public FamilyController(FamilyService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "获取当前家庭", description = "返回当前登录用户所在家庭")
    public ApiResponse<Family> current() {
        return ApiResponse.ok(service.currentFamily());
    }

    @GetMapping("/members")
    @Operation(summary = "查询家庭成员", description = "返回当前家庭成员列表")
    public ApiResponse<List<MemberResponse>> members() {
        return ApiResponse.ok(service.members());
    }

    @PutMapping("/members/{id}")
    @Operation(summary = "更新家庭成员", description = "更新家庭成员角色、显示名称或状态")
    public ApiResponse<MemberResponse> updateMember(@Parameter(description = "家庭成员 ID", required = true) @PathVariable Long id,
                                                    @Valid @RequestBody UpdateMemberRequest request) {
        return ApiResponse.ok(service.updateMember(id, request));
    }
}
