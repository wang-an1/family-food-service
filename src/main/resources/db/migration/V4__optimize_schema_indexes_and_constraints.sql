alter database character set utf8mb4 collate utf8mb4_0900_ai_ci;

alter table `family` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `user` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `family_member` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `dish_category` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `dish` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `dish_tag` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `dish_tag_relation` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `dish_ingredient` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `meal_session` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `personal_order` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `order_item` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `order_status_log` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `intent_request` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `ai_task` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `ai_source_content` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `ai_extracted_dish` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `ai_recommendation` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `shopping_list` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `shopping_list_item` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `system_config` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;
alter table `ai_call_log` convert to character set utf8mb4 collate utf8mb4_0900_ai_ci;

alter table `family`
  modify column `id` bigint unsigned not null auto_increment comment '家庭ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间',
  modify column `updated_at` datetime not null default current_timestamp on update current_timestamp comment '更新时间';

alter table `user`
  modify column `id` bigint unsigned not null auto_increment comment '用户ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间',
  modify column `updated_at` datetime not null default current_timestamp on update current_timestamp comment '更新时间';

alter table `family_member`
  modify column `id` bigint unsigned not null auto_increment comment '成员关系ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID',
  modify column `user_id` bigint unsigned not null comment '用户ID';

alter table `dish_category`
  modify column `id` bigint unsigned not null auto_increment comment '分类ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间',
  modify column `updated_at` datetime not null default current_timestamp on update current_timestamp comment '更新时间';

alter table `dish`
  modify column `id` bigint unsigned not null auto_increment comment '菜品ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID',
  modify column `category_id` bigint unsigned comment '菜品分类ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间',
  modify column `updated_at` datetime not null default current_timestamp on update current_timestamp comment '更新时间',
  modify column `created_by` bigint unsigned comment '创建人用户ID',
  modify column `updated_by` bigint unsigned comment '最后更新人用户ID';

alter table `dish_tag`
  modify column `id` bigint unsigned not null auto_increment comment '标签ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID';

alter table `dish_tag_relation`
  modify column `id` bigint unsigned not null auto_increment comment '关联ID',
  modify column `dish_id` bigint unsigned not null comment '菜品ID',
  modify column `tag_id` bigint unsigned not null comment '标签ID';

alter table `dish_ingredient`
  modify column `id` bigint unsigned not null auto_increment comment '食材ID',
  modify column `dish_id` bigint unsigned not null comment '菜品ID';

alter table `meal_session`
  modify column `id` bigint unsigned not null auto_increment comment '餐次ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间',
  modify column `updated_at` datetime not null default current_timestamp on update current_timestamp comment '更新时间',
  modify column `created_by` bigint unsigned comment '创建人用户ID';

alter table `personal_order`
  modify column `id` bigint unsigned not null auto_increment comment '订单ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID',
  modify column `meal_session_id` bigint unsigned not null comment '餐次ID',
  modify column `user_id` bigint unsigned not null comment '下单用户ID',
  modify column `confirmed_by` bigint unsigned comment '确认人用户ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间',
  modify column `updated_at` datetime not null default current_timestamp on update current_timestamp comment '更新时间';

alter table `order_item`
  modify column `id` bigint unsigned not null auto_increment comment '订单明细ID',
  modify column `order_id` bigint unsigned not null comment '订单ID',
  modify column `dish_id` bigint unsigned not null comment '菜品ID';

alter table `order_status_log`
  modify column `id` bigint unsigned not null auto_increment comment '日志ID',
  modify column `order_id` bigint unsigned not null comment '订单ID',
  modify column `operator_id` bigint unsigned comment '操作人用户ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间';

alter table `intent_request`
  modify column `id` bigint unsigned not null auto_increment comment '意图请求ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID',
  modify column `user_id` bigint unsigned not null comment '提交用户ID',
  modify column `ai_task_id` bigint unsigned comment '关联AI任务ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间',
  modify column `updated_at` datetime not null default current_timestamp on update current_timestamp comment '更新时间';

alter table `ai_task`
  modify column `id` bigint unsigned not null auto_increment comment 'AI任务ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID',
  modify column `user_id` bigint unsigned not null comment '发起用户ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间',
  modify column `updated_at` datetime not null default current_timestamp on update current_timestamp comment '更新时间';

alter table `ai_source_content`
  modify column `id` bigint unsigned not null auto_increment comment '来源内容ID',
  modify column `ai_task_id` bigint unsigned not null comment 'AI任务ID';

alter table `ai_extracted_dish`
  modify column `id` bigint unsigned not null auto_increment comment 'AI提取菜品ID',
  modify column `ai_task_id` bigint unsigned not null comment 'AI任务ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID',
  modify column `match_dish_id` bigint unsigned comment '匹配到的已有菜品ID',
  modify column `converted_dish_id` bigint unsigned comment '审核通过后转换成的菜品ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间',
  modify column `updated_at` datetime not null default current_timestamp on update current_timestamp comment '更新时间';

alter table `ai_recommendation`
  modify column `id` bigint unsigned not null auto_increment comment 'AI推荐ID',
  modify column `ai_task_id` bigint unsigned comment 'AI任务ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID',
  modify column `user_id` bigint unsigned not null comment '请求用户ID',
  modify column `dish_id` bigint unsigned comment '推荐的已有菜品ID',
  modify column `extracted_dish_id` bigint unsigned comment '推荐的AI提取菜品ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间';

alter table `shopping_list`
  modify column `id` bigint unsigned not null auto_increment comment '采购清单ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID',
  modify column `meal_session_id` bigint unsigned not null comment '餐次ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间',
  modify column `updated_at` datetime not null default current_timestamp on update current_timestamp comment '更新时间';

alter table `shopping_list_item`
  modify column `id` bigint unsigned not null auto_increment comment '采购清单明细ID',
  modify column `shopping_list_id` bigint unsigned not null comment '采购清单ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间',
  modify column `updated_at` datetime not null default current_timestamp on update current_timestamp comment '更新时间';

alter table `system_config`
  modify column `id` bigint unsigned not null auto_increment comment '配置ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID',
  modify column `updated_at` datetime not null default current_timestamp on update current_timestamp comment '更新时间';

alter table `ai_call_log`
  modify column `id` bigint unsigned not null auto_increment comment 'AI调用日志ID',
  modify column `family_id` bigint unsigned not null comment '家庭ID',
  modify column `user_id` bigint unsigned comment '调用用户ID',
  modify column `ai_task_id` bigint unsigned comment 'AI任务ID',
  modify column `created_at` datetime not null default current_timestamp comment '创建时间';

alter table `family_member`
  add index `idx_family_member_user_status` (`user_id`, `status`);

alter table `dish_category`
  add index `idx_dish_category_family_sort` (`family_id`, `sort_order`, `id`);

alter table `dish`
  add index `idx_dish_family_deleted_updated` (`family_id`, `deleted`, `updated_at` desc, `id` desc),
  add index `idx_dish_family_deleted_status_category_updated` (`family_id`, `deleted`, `status`, `category_id`, `updated_at` desc, `id` desc),
  add index `idx_dish_family_deleted_category_updated` (`family_id`, `deleted`, `category_id`, `updated_at` desc, `id` desc),
  add index `idx_dish_created_by` (`created_by`),
  add index `idx_dish_updated_by` (`updated_by`);

alter table `dish_tag`
  add index `idx_dish_tag_family_id` (`family_id`, `id`);

alter table `dish_tag_relation`
  add index `idx_dish_tag_relation_tag_dish` (`tag_id`, `dish_id`);

alter table `meal_session`
  add index `idx_meal_session_family_status_date_id` (`family_id`, `status`, `meal_date` desc, `id` desc),
  add index `idx_meal_session_created_by` (`created_by`);

alter table `personal_order`
  add index `idx_order_family_deleted_created` (`family_id`, `deleted`, `created_at` desc, `id` desc),
  add index `idx_order_family_deleted_session_created` (`family_id`, `deleted`, `meal_session_id`, `created_at` desc, `id` desc),
  add index `idx_order_family_deleted_status_created` (`family_id`, `deleted`, `status`, `created_at` desc, `id` desc),
  add index `idx_order_family_deleted_user_created` (`family_id`, `deleted`, `user_id`, `created_at` desc, `id` desc),
  add index `idx_order_family_session_status` (`family_id`, `meal_session_id`, `status`),
  add index `idx_order_confirmed_by` (`confirmed_by`);

alter table `order_status_log`
  add index `idx_order_status_log_order_created` (`order_id`, `created_at`),
  add index `idx_order_status_log_operator` (`operator_id`);

alter table `intent_request`
  add index `idx_intent_family_user_created` (`family_id`, `user_id`, `created_at` desc, `id` desc),
  add index `idx_intent_ai_task` (`ai_task_id`);

alter table `ai_task`
  add index `idx_ai_task_family_created` (`family_id`, `created_at` desc, `id` desc),
  add index `idx_ai_task_family_user_created` (`family_id`, `user_id`, `created_at` desc, `id` desc),
  add index `idx_ai_task_family_status_created` (`family_id`, `status`, `created_at` desc, `id` desc),
  add index `idx_ai_task_family_source_created` (`family_id`, `source_type`, `created_at` desc, `id` desc);

alter table `ai_source_content`
  add index `idx_ai_source_content_task` (`ai_task_id`);

alter table `ai_extracted_dish`
  add index `idx_ai_extracted_task` (`ai_task_id`),
  add index `idx_ai_extracted_family_created` (`family_id`, `created_at` desc, `id` desc),
  add index `idx_ai_extracted_family_review_created` (`family_id`, `review_status`, `created_at` desc, `id` desc),
  add index `idx_ai_extracted_match_dish` (`match_dish_id`),
  add index `idx_ai_extracted_converted_dish` (`converted_dish_id`);

alter table `ai_recommendation`
  add index `idx_ai_recommendation_task` (`ai_task_id`),
  add index `idx_ai_recommendation_family_user_created` (`family_id`, `user_id`, `created_at` desc, `id` desc),
  add index `idx_ai_recommendation_dish` (`dish_id`),
  add index `idx_ai_recommendation_extracted` (`extracted_dish_id`);

alter table `shopping_list`
  add index `idx_shopping_list_family_session` (`family_id`, `meal_session_id`);

alter table `shopping_list_item`
  add index `idx_shopping_item_list_checked_category_name` (`shopping_list_id`, `checked`, `category`, `name`),
  add index `idx_shopping_item_list_source` (`shopping_list_id`, `source`);

alter table `ai_call_log`
  add index `idx_ai_call_log_family_created` (`family_id`, `created_at` desc, `id` desc),
  add index `idx_ai_call_log_user_created` (`user_id`, `created_at` desc, `id` desc),
  add index `idx_ai_call_log_task` (`ai_task_id`);

alter table `family_member`
  add constraint `fk_family_member_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict,
  add constraint `fk_family_member_user` foreign key (`user_id`) references `user` (`id`) on delete restrict on update restrict;

alter table `dish_category`
  add constraint `fk_dish_category_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict;

alter table `dish`
  add constraint `fk_dish_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict,
  add constraint `fk_dish_category` foreign key (`category_id`) references `dish_category` (`id`) on delete restrict on update restrict,
  add constraint `fk_dish_created_by` foreign key (`created_by`) references `user` (`id`) on delete restrict on update restrict,
  add constraint `fk_dish_updated_by` foreign key (`updated_by`) references `user` (`id`) on delete restrict on update restrict;

alter table `dish_tag`
  add constraint `fk_dish_tag_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict;

alter table `dish_tag_relation`
  add constraint `fk_dish_tag_relation_dish` foreign key (`dish_id`) references `dish` (`id`) on delete restrict on update restrict,
  add constraint `fk_dish_tag_relation_tag` foreign key (`tag_id`) references `dish_tag` (`id`) on delete restrict on update restrict;

alter table `dish_ingredient`
  add constraint `fk_dish_ingredient_dish` foreign key (`dish_id`) references `dish` (`id`) on delete restrict on update restrict;

alter table `meal_session`
  add constraint `fk_meal_session_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict,
  add constraint `fk_meal_session_created_by` foreign key (`created_by`) references `user` (`id`) on delete restrict on update restrict;

alter table `personal_order`
  add constraint `fk_personal_order_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict,
  add constraint `fk_personal_order_session` foreign key (`meal_session_id`) references `meal_session` (`id`) on delete restrict on update restrict,
  add constraint `fk_personal_order_user` foreign key (`user_id`) references `user` (`id`) on delete restrict on update restrict,
  add constraint `fk_personal_order_confirmed_by` foreign key (`confirmed_by`) references `user` (`id`) on delete restrict on update restrict;

alter table `order_item`
  add constraint `fk_order_item_order` foreign key (`order_id`) references `personal_order` (`id`) on delete restrict on update restrict,
  add constraint `fk_order_item_dish` foreign key (`dish_id`) references `dish` (`id`) on delete restrict on update restrict;

alter table `order_status_log`
  add constraint `fk_order_status_log_order` foreign key (`order_id`) references `personal_order` (`id`) on delete restrict on update restrict,
  add constraint `fk_order_status_log_operator` foreign key (`operator_id`) references `user` (`id`) on delete restrict on update restrict;

alter table `ai_task`
  add constraint `fk_ai_task_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict,
  add constraint `fk_ai_task_user` foreign key (`user_id`) references `user` (`id`) on delete restrict on update restrict;

alter table `intent_request`
  add constraint `fk_intent_request_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict,
  add constraint `fk_intent_request_user` foreign key (`user_id`) references `user` (`id`) on delete restrict on update restrict,
  add constraint `fk_intent_request_ai_task` foreign key (`ai_task_id`) references `ai_task` (`id`) on delete restrict on update restrict;

alter table `ai_source_content`
  add constraint `fk_ai_source_content_task` foreign key (`ai_task_id`) references `ai_task` (`id`) on delete restrict on update restrict;

alter table `ai_extracted_dish`
  add constraint `fk_ai_extracted_task` foreign key (`ai_task_id`) references `ai_task` (`id`) on delete restrict on update restrict,
  add constraint `fk_ai_extracted_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict,
  add constraint `fk_ai_extracted_match_dish` foreign key (`match_dish_id`) references `dish` (`id`) on delete restrict on update restrict,
  add constraint `fk_ai_extracted_converted_dish` foreign key (`converted_dish_id`) references `dish` (`id`) on delete restrict on update restrict;

alter table `ai_recommendation`
  add constraint `fk_ai_recommendation_task` foreign key (`ai_task_id`) references `ai_task` (`id`) on delete restrict on update restrict,
  add constraint `fk_ai_recommendation_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict,
  add constraint `fk_ai_recommendation_user` foreign key (`user_id`) references `user` (`id`) on delete restrict on update restrict,
  add constraint `fk_ai_recommendation_dish` foreign key (`dish_id`) references `dish` (`id`) on delete restrict on update restrict,
  add constraint `fk_ai_recommendation_extracted` foreign key (`extracted_dish_id`) references `ai_extracted_dish` (`id`) on delete restrict on update restrict;

alter table `shopping_list`
  add constraint `fk_shopping_list_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict,
  add constraint `fk_shopping_list_session` foreign key (`meal_session_id`) references `meal_session` (`id`) on delete restrict on update restrict;

alter table `shopping_list_item`
  add constraint `fk_shopping_list_item_list` foreign key (`shopping_list_id`) references `shopping_list` (`id`) on delete restrict on update restrict;

alter table `system_config`
  add constraint `fk_system_config_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict;

alter table `ai_call_log`
  add constraint `fk_ai_call_log_family` foreign key (`family_id`) references `family` (`id`) on delete restrict on update restrict,
  add constraint `fk_ai_call_log_user` foreign key (`user_id`) references `user` (`id`) on delete restrict on update restrict,
  add constraint `fk_ai_call_log_task` foreign key (`ai_task_id`) references `ai_task` (`id`) on delete restrict on update restrict;
