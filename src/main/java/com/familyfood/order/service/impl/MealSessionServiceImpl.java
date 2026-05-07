package com.familyfood.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.familyfood.common.AppException;
import com.familyfood.common.context.ActorContext;
import com.familyfood.common.StatusValues;
import com.familyfood.order.dao.MealSessionMapper;
import com.familyfood.order.dto.MealSessionRequest;
import com.familyfood.order.entity.MealSession;
import com.familyfood.order.service.MealSessionService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MealSessionServiceImpl implements MealSessionService {
    private final MealSessionMapper mealSessionMapper;

    @Autowired
    public MealSessionServiceImpl(MealSessionMapper mealSessionMapper) {
        this.mealSessionMapper = mealSessionMapper;
    }

    @Override
    public List<MealSession> sessions(ActorContext actor) {
        return mealSessionMapper.selectList(new QueryWrapper<MealSession>()
                .eq("family_id", actor.familyId())
                .orderByDesc("meal_date", "id"));
    }

    @Override
    public MealSession current(ActorContext actor) {
        MealSession session = mealSessionMapper.selectOne(new QueryWrapper<MealSession>()
                .eq("family_id", actor.familyId())
                .eq("status", "OPEN")
                .ge("meal_date", LocalDate.now().minusDays(1))
                .orderByDesc("meal_date", "id")
                .last("limit 1"));
        if (session == null) {
            throw AppException.notFound("当前还没有开放中的餐次，请稍后再试");
        }
        return session;
    }

    @Override
    @Transactional
    public MealSession create(ActorContext actor, MealSessionRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String mealType = StatusValues.required(request.mealType(), StatusValues.MEAL_TYPES, "mealType");
        String status = StatusValues.orDefault(request.status(), "OPEN", StatusValues.MEAL_STATUSES, "status");
        MealSession session = new MealSession();
        session.setFamilyId(actor.familyId());
        session.setTitle(request.title());
        session.setMealType(mealType);
        session.setMealDate(request.mealDate() == null ? LocalDate.now() : request.mealDate());
        session.setExpectedTime(request.expectedTime());
        session.setStatus(status);
        session.setConfirmRequired(Boolean.FALSE.equals(request.confirmRequired()) ? 0 : 1);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        session.setCreatedBy(actor.userId());
        mealSessionMapper.insert(session);
        return session;
    }

    @Override
    public MealSession requireFamilySession(ActorContext actor, Long mealSessionId) {
        MealSession session = mealSessionMapper.selectById(mealSessionId);
        if (session == null || !Objects.equals(session.getFamilyId(), actor.familyId())) {
            throw AppException.notFound("未找到这个餐次，请刷新后再试");
        }
        return session;
    }
}
