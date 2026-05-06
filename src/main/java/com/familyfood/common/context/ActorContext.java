package com.familyfood.common.context;

import com.familyfood.common.AppException;

public record ActorContext(Long familyId, Long userId, String role, boolean admin) {
    public void requireAdmin() {
        if (!admin) {
            throw AppException.forbidden("Only administrators can perform this operation");
        }
    }
}
