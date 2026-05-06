package com.familyfood.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyfood.family.api.FamilyMembershipApi;
import com.familyfood.auth.dao.UserMapper;
import com.familyfood.auth.dto.LoginRequest;
import com.familyfood.auth.dto.LoginResponse;
import com.familyfood.auth.dto.MeResponse;
import com.familyfood.auth.entity.User;
import com.familyfood.auth.security.UserPrincipal;
import com.familyfood.common.AppException;
import com.familyfood.common.context.ActorContext;
import com.familyfood.family.entity.FamilyMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserMapper userMapper;
    private final FamilyMembershipApi membershipApi;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserMapper userMapper, FamilyMembershipApi membershipApi, PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userMapper = userMapper;
        this.membershipApi = membershipApi;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (user == null || !"ACTIVE".equals(user.getStatus()) || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.info("login_failed username={} reason=bad_credentials_or_inactive", request.username());
            throw AppException.unauthorized("用户名或密码错误");
        }
        FamilyMember member = membershipApi.activeMembershipForUser(user.getId());
        if (member == null) {
            log.info("login_failed username={} userId={} reason=no_active_family", user.getUsername(), user.getId());
            throw AppException.forbidden("用户未加入家庭");
        }
        UserPrincipal principal = new UserPrincipal(user.getId(), member.getFamilyId(), user.getUsername(), user.getNickname(), member.getRole());
        log.info("login_success username={} userId={} familyId={} role={}",
                user.getUsername(), user.getId(), member.getFamilyId(), member.getRole());
        return new LoginResponse(jwtService.generate(principal), new MeResponse(user.getId(), user.getUsername(), user.getNickname(), member.getFamilyId(), member.getRole()));
    }

    public MeResponse me(ActorContext actor) {
        User user = userMapper.selectById(actor.userId());
        return new MeResponse(actor.userId(), user == null ? null : user.getUsername(),
                user == null ? null : user.getNickname(), actor.familyId(), actor.role());
    }
}
