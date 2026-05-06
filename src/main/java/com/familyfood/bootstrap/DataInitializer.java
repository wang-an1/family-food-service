package com.familyfood.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.familyfood.auth.dao.UserMapper;
import com.familyfood.auth.entity.User;
import com.familyfood.config.AppProperties;
import com.familyfood.dish.dao.DishCategoryMapper;
import com.familyfood.dish.dao.DishIngredientMapper;
import com.familyfood.dish.dao.DishMapper;
import com.familyfood.dish.dao.DishTagMapper;
import com.familyfood.dish.dao.DishTagRelationMapper;
import com.familyfood.dish.entity.Dish;
import com.familyfood.dish.entity.DishCategory;
import com.familyfood.dish.entity.DishIngredient;
import com.familyfood.dish.entity.DishTag;
import com.familyfood.dish.entity.DishTagRelation;
import com.familyfood.family.dao.FamilyMapper;
import com.familyfood.family.dao.FamilyMemberMapper;
import com.familyfood.family.entity.Family;
import com.familyfood.family.entity.FamilyMember;
import com.familyfood.order.dao.MealSessionMapper;
import com.familyfood.order.entity.MealSession;
import com.familyfood.system.dao.SystemConfigMapper;
import com.familyfood.system.entity.SystemConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    private final AppProperties properties;
    private final PasswordEncoder passwordEncoder;
    private final FamilyMapper familyMapper;
    private final UserMapper userMapper;
    private final FamilyMemberMapper memberMapper;
    private final DishCategoryMapper categoryMapper;
    private final DishTagMapper tagMapper;
    private final DishMapper dishMapper;
    private final DishIngredientMapper ingredientMapper;
    private final DishTagRelationMapper relationMapper;
    private final MealSessionMapper mealSessionMapper;
    private final SystemConfigMapper configMapper;

    @Autowired
    public DataInitializer(AppProperties properties, PasswordEncoder passwordEncoder, FamilyMapper familyMapper,
                           UserMapper userMapper, FamilyMemberMapper memberMapper, DishCategoryMapper categoryMapper,
                           DishTagMapper tagMapper, DishMapper dishMapper, DishIngredientMapper ingredientMapper,
                           DishTagRelationMapper relationMapper, MealSessionMapper mealSessionMapper,
                           SystemConfigMapper configMapper) {
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
        this.familyMapper = familyMapper;
        this.userMapper = userMapper;
        this.memberMapper = memberMapper;
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.dishMapper = dishMapper;
        this.ingredientMapper = ingredientMapper;
        this.relationMapper = relationMapper;
        this.mealSessionMapper = mealSessionMapper;
        this.configMapper = configMapper;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (familyMapper.selectCount(null) > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Family family = new Family();
        family.setName("我的家庭");
        family.setInviteCode("HOME2026");
        family.setStatus("ACTIVE");
        family.setCreatedAt(now);
        family.setUpdatedAt(now);
        familyMapper.insert(family);

        User admin = createUser("admin", properties.initAdminPassword(), "家庭管理员", now);
        User member = createUser("member", "member123", "家庭成员", now);
        createMember(family.getId(), admin.getId(), "ADMIN", "管理员", now);
        createMember(family.getId(), member.getId(), "MEMBER", "成员", now);

        Map<String, DishCategory> categories = createCategories(family.getId(), now);
        Map<String, DishTag> tags = createTags(family.getId());
        seedDishes(family.getId(), admin.getId(), categories, tags, now);
        seedConfigs(family.getId(), now);
        seedTodayDinner(family.getId(), admin.getId(), now);
    }

    private User createUser(String username, String password, String nickname, LocalDateTime now) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setStatus("ACTIVE");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        return user;
    }

    private void createMember(Long familyId, Long userId, String role, String displayName, LocalDateTime now) {
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        member.setRole(role);
        member.setDisplayName(displayName);
        member.setStatus("ACTIVE");
        member.setJoinedAt(now);
        memberMapper.insert(member);
    }

    private Map<String, DishCategory> createCategories(Long familyId, LocalDateTime now) {
        List<String> names = List.of("家常菜", "汤羹", "主食", "小吃");
        java.util.LinkedHashMap<String, DishCategory> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < names.size(); i++) {
            DishCategory category = new DishCategory();
            category.setFamilyId(familyId);
            category.setName(names.get(i));
            category.setSortOrder(i + 1);
            category.setCreatedAt(now);
            category.setUpdatedAt(now);
            categoryMapper.insert(category);
            map.put(category.getName(), category);
        }
        return map;
    }

    private Map<String, DishTag> createTags(Long familyId) {
        List<String> names = List.of("快手菜", "儿童可吃", "清淡", "少油", "下饭菜");
        List<String> colors = List.of("#2f7d57", "#c06c3d", "#2f6f9f", "#7a6a36", "#9a3f46");
        java.util.LinkedHashMap<String, DishTag> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < names.size(); i++) {
            DishTag tag = new DishTag();
            tag.setFamilyId(familyId);
            tag.setName(names.get(i));
            tag.setColor(colors.get(i));
            tagMapper.insert(tag);
            map.put(tag.getName(), tag);
        }
        return map;
    }

    private void seedDishes(Long familyId, Long adminId, Map<String, DishCategory> categories, Map<String, DishTag> tags, LocalDateTime now) {
        Dish tomatoEgg = createDish(familyId, categories.get("家常菜").getId(), "番茄炒蛋", "酸甜,清淡",
                "酸甜", "LUNCH,DINNER", 15, "番茄切块，鸡蛋炒散后与番茄同炒调味。", adminId, now);
        addIngredient(tomatoEgg.getId(), "番茄", "蔬菜", new BigDecimal("2"), "个");
        addIngredient(tomatoEgg.getId(), "鸡蛋", "蛋类", new BigDecimal("3"), "个");
        tag(tomatoEgg.getId(), tags.get("快手菜").getId(), tags.get("儿童可吃").getId(), tags.get("清淡").getId());

        Dish soup = createDish(familyId, categories.get("汤羹").getId(), "青菜豆腐汤", "清淡,汤",
                "清淡", "LUNCH,DINNER", 20, "豆腐切块，青菜洗净，清汤煮开后调味。", adminId, now);
        addIngredient(soup.getId(), "青菜", "蔬菜", new BigDecimal("300"), "克");
        addIngredient(soup.getId(), "豆腐", "豆制品", new BigDecimal("1"), "块");
        tag(soup.getId(), tags.get("清淡").getId(), tags.get("少油").getId());

        Dish wings = createDish(familyId, categories.get("家常菜").getId(), "可乐鸡翅", "鸡翅",
                "咸甜", "LUNCH,DINNER", 35, "鸡翅煎香后加入可乐和生抽，小火收汁。", adminId, now);
        addIngredient(wings.getId(), "鸡翅", "肉类", new BigDecimal("500"), "克");
        addIngredient(wings.getId(), "可乐", "饮料", new BigDecimal("1"), "听");
        tag(wings.getId(), tags.get("儿童可吃").getId(), tags.get("下饭菜").getId());
    }

    private Dish createDish(Long familyId, Long categoryId, String name, String description, String taste,
                            String mealTypes, int minutes, String instructions, Long adminId, LocalDateTime now) {
        Dish dish = new Dish();
        dish.setFamilyId(familyId);
        dish.setCategoryId(categoryId);
        dish.setName(name);
        dish.setDescription(description);
        dish.setTaste(taste);
        dish.setMealTypes(mealTypes);
        dish.setDifficulty("EASY");
        dish.setEstimatedMinutes(minutes);
        dish.setDefaultServings(BigDecimal.ONE);
        dish.setInstructions(instructions);
        dish.setSourceType("MANUAL");
        dish.setStatus("ACTIVE");
        dish.setCreatedAt(now);
        dish.setUpdatedAt(now);
        dish.setCreatedBy(adminId);
        dish.setUpdatedBy(adminId);
        dish.setDeleted(0);
        dishMapper.insert(dish);
        return dish;
    }

    private void addIngredient(Long dishId, String name, String category, BigDecimal amount, String unit) {
        DishIngredient ingredient = new DishIngredient();
        ingredient.setDishId(dishId);
        ingredient.setName(name);
        ingredient.setCategory(category);
        ingredient.setAmount(amount);
        ingredient.setUnit(unit);
        ingredient.setRequired(1);
        ingredientMapper.insert(ingredient);
    }

    private void tag(Long dishId, Long... tagIds) {
        for (Long tagId : tagIds) {
            DishTagRelation relation = new DishTagRelation();
            relation.setDishId(dishId);
            relation.setTagId(tagId);
            relationMapper.insert(relation);
        }
    }

    private void seedConfigs(Long familyId, LocalDateTime now) {
        List<SystemConfig> configs = new ArrayList<>();
        configs.add(config(familyId, "order.confirm_required", "true", "BOOLEAN", 0, now));
        configs.add(config(familyId, "ai.enabled", "true", "BOOLEAN", 0, now));
        configs.add(config(familyId, "ai.provider", properties.aiProvider(), "STRING", 0, now));
        configs.add(config(familyId, "ai.base_url", properties.ai().baseUrl(), "STRING", 0, now));
        configs.add(config(familyId, "ai.chat_model", properties.ai().chatModel(), "STRING", 0, now));
        configs.add(config(familyId, "ai.link_parse_enabled", "true", "BOOLEAN", 0, now));
        configs.add(config(familyId, "ai.require_admin_review_for_dish", "true", "BOOLEAN", 0, now));
        if (properties.ai().apiKey() != null && !properties.ai().apiKey().isBlank()) {
            configs.add(config(familyId, "ai.api_key", properties.ai().apiKey(), "STRING", 1, now));
        }
        configs.forEach(configMapper::insert);
    }

    private SystemConfig config(Long familyId, String key, String value, String type, int encrypted, LocalDateTime now) {
        SystemConfig config = new SystemConfig();
        config.setFamilyId(familyId);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setValueType(type);
        config.setEncrypted(encrypted);
        config.setUpdatedAt(now);
        return config;
    }

    private void seedTodayDinner(Long familyId, Long adminId, LocalDateTime now) {
        if (mealSessionMapper.selectCount(new QueryWrapper<MealSession>().eq("family_id", familyId)) > 0) {
            return;
        }
        MealSession session = new MealSession();
        session.setFamilyId(familyId);
        session.setTitle("今天晚餐");
        session.setMealType("DINNER");
        session.setMealDate(LocalDate.now());
        session.setExpectedTime(LocalDate.now().atTime(19, 0));
        session.setStatus("OPEN");
        session.setConfirmRequired(1);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        session.setCreatedBy(adminId);
        mealSessionMapper.insert(session);
    }
}
