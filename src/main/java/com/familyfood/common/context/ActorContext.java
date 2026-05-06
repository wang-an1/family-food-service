package com.familyfood.common.context;

import com.familyfood.common.AppException;

public record ActorContext(Long familyId, Long userId, String role, boolean admin) {
    public void requireAdmin() {
        if (!admin) {
            throw AppException.forbidden("仅管理员可操作");
        }
    }
}
