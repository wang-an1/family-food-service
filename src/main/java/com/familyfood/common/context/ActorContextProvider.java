package com.familyfood.common.context;

import com.familyfood.auth.security.CurrentUser;
import com.familyfood.auth.security.UserPrincipal;
import com.familyfood.common.Enums.Role;
import org.springframework.stereotype.Component;

@Component
public class ActorContextProvider {
    public ActorContext current() {
        UserPrincipal user = CurrentUser.get();
        boolean admin = Role.ADMIN.name().equals(user.role());
        return new ActorContext(user.familyId(), user.userId(), user.role(), admin);
    }
}
