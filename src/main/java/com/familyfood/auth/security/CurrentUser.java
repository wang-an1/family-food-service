package com.familyfood.auth.security;

import com.familyfood.common.AppException;
import com.familyfood.common.Enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static UserPrincipal get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw AppException.unauthorized("请先登录后再继续操作");
        }
        return principal;
    }

    public static boolean isAdmin() {
        return Role.ADMIN.name().equals(get().role());
    }

    public static void requireAdmin() {
        if (!isAdmin()) {
            throw AppException.forbidden("只有管理员可以进行此操作");
        }
    }
}
