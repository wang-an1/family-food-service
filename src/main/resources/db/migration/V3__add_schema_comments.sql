alter table `family` comment = '家庭信息表，记录一个家庭空间的基础资料';
alter table `family`
  modify column `id` bigint not null auto_increment comment '家庭ID',
  modify column `name` varchar(64) not null comment '家庭名称',
  modify column `invite_code` varchar(32) comment '家庭邀请码，用于成员加入',
  modify column `status` varchar(16) not null comment '家庭状态，如ACTIVE',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间';

alter table `user` comment = '用户账号表，记录登录账号和个人资料';
alter table `user`
  modify column `id` bigint not null auto_increment comment '用户ID',
  modify column `username` varchar(64) not null comment '登录用户名',
  modify column `password_hash` varchar(128) not null comment 'BCrypt密码哈希',
  modify column `nickname` varchar(64) not null comment '用户昵称',
  modify column `avatar_url` varchar(255) comment '头像URL',
  modify column `phone` varchar(32) comment '联系电话',
  modify column `status` varchar(16) not null comment '用户状态，如ACTIVE',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间';

alter table `family_member` comment = '家庭成员关系表，记录用户在家庭中的角色';
alter table `family_member`
  modify column `id` bigint not null auto_increment comment '成员关系ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `user_id` bigint not null comment '用户ID',
  modify column `role` varchar(16) not null comment '家庭角色，如ADMIN或MEMBER',
  modify column `display_name` varchar(64) comment '成员在家庭中的显示名称',
  modify column `status` varchar(16) not null comment '成员状态',
  modify column `joined_at` datetime not null comment '加入家庭时间';

alter table `dish_category` comment = '菜品分类表，按家庭维护菜品分类';
alter table `dish_category`
  modify column `id` bigint not null auto_increment comment '分类ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `name` varchar(64) not null comment '分类名称',
  modify column `sort_order` int not null default 0 comment '排序值，越小越靠前',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间';

alter table `dish` comment = '菜品主表，记录家庭菜谱和AI转换后的菜品';
alter table `dish`
  modify column `id` bigint not null auto_increment comment '菜品ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `category_id` bigint comment '菜品分类ID',
  modify column `name` varchar(128) not null comment '菜品名称',
  modify column `aliases` varchar(255) comment '菜品别名，多个值用逗号分隔',
  modify column `description` varchar(1000) comment '菜品描述',
  modify column `image_url` varchar(255) comment '菜品图片URL',
  modify column `taste` varchar(128) comment '口味标签描述',
  modify column `meal_types` varchar(128) comment '适用餐别，多个值用逗号分隔',
  modify column `difficulty` varchar(16) comment '制作难度',
  modify column `estimated_minutes` int comment '预计制作分钟数',
  modify column `default_servings` decimal(6,2) not null default 1 comment '默认份数',
  modify column `instructions` text comment '制作步骤',
  modify column `source_type` varchar(32) not null comment '来源类型，如MANUAL、AI、LINK',
  modify column `source_url` varchar(1000) comment '来源链接',
  modify column `status` varchar(16) not null comment '菜品状态，如ACTIVE或INACTIVE',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间',
  modify column `created_by` bigint comment '创建人用户ID',
  modify column `updated_by` bigint comment '最后更新人用户ID',
  modify column `deleted` tinyint not null default 0 comment '逻辑删除标记，0未删除，1已删除';

alter table `dish_tag` comment = '菜品标签表，记录家庭自定义菜品标签';
alter table `dish_tag`
  modify column `id` bigint not null auto_increment comment '标签ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `name` varchar(64) not null comment '标签名称',
  modify column `color` varchar(16) comment '标签颜色';

alter table `dish_tag_relation` comment = '菜品与标签关联表';
alter table `dish_tag_relation`
  modify column `id` bigint not null auto_increment comment '关联ID',
  modify column `dish_id` bigint not null comment '菜品ID',
  modify column `tag_id` bigint not null comment '标签ID';

alter table `dish_ingredient` comment = '菜品食材表，记录单个菜品需要的食材';
alter table `dish_ingredient`
  modify column `id` bigint not null auto_increment comment '食材ID',
  modify column `dish_id` bigint not null comment '菜品ID',
  modify column `name` varchar(128) not null comment '食材名称',
  modify column `amount` decimal(10,2) comment '食材数量',
  modify column `unit` varchar(32) comment '食材单位',
  modify column `category` varchar(64) comment '食材分类',
  modify column `required` tinyint not null default 1 comment '是否必需，1必需，0可选',
  modify column `note` varchar(255) comment '食材备注';

alter table `meal_session` comment = '餐次表，记录一次家庭点餐或用餐安排';
alter table `meal_session`
  modify column `id` bigint not null auto_increment comment '餐次ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `title` varchar(128) not null comment '餐次标题',
  modify column `meal_type` varchar(16) not null comment '餐别，如BREAKFAST、LUNCH、DINNER',
  modify column `meal_date` date not null comment '用餐日期',
  modify column `expected_time` datetime comment '预计用餐时间',
  modify column `status` varchar(16) not null comment '餐次状态',
  modify column `confirm_required` tinyint not null comment '是否需要管理员确认订单，1需要，0不需要',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间',
  modify column `created_by` bigint comment '创建人用户ID';

alter table `personal_order` comment = '个人点餐订单表，记录成员在某个餐次下的点餐';
alter table `personal_order`
  modify column `id` bigint not null auto_increment comment '订单ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `meal_session_id` bigint not null comment '餐次ID',
  modify column `user_id` bigint not null comment '下单用户ID',
  modify column `status` varchar(32) not null comment '订单状态',
  modify column `note` varchar(500) comment '订单备注',
  modify column `avoidances` varchar(500) comment '忌口或特殊要求',
  modify column `expected_time` datetime comment '期望用餐时间',
  modify column `submitted_at` datetime comment '提交时间',
  modify column `confirmed_at` datetime comment '确认时间',
  modify column `confirmed_by` bigint comment '确认人用户ID',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间',
  modify column `deleted` tinyint not null default 0 comment '逻辑删除标记，0未删除，1已删除';

alter table `order_item` comment = '订单明细表，记录订单中的菜品和数量';
alter table `order_item`
  modify column `id` bigint not null auto_increment comment '订单明细ID',
  modify column `order_id` bigint not null comment '订单ID',
  modify column `dish_id` bigint not null comment '菜品ID',
  modify column `dish_name_snapshot` varchar(128) not null comment '下单时的菜品名称快照',
  modify column `quantity` decimal(8,2) not null comment '点餐数量',
  modify column `unit` varchar(32) not null default '份' comment '数量单位',
  modify column `note` varchar(255) comment '明细备注';

alter table `order_status_log` comment = '订单状态流转日志表';
alter table `order_status_log`
  modify column `id` bigint not null auto_increment comment '日志ID',
  modify column `order_id` bigint not null comment '订单ID',
  modify column `from_status` varchar(32) comment '变更前状态',
  modify column `to_status` varchar(32) not null comment '变更后状态',
  modify column `operator_id` bigint comment '操作人用户ID',
  modify column `reason` varchar(255) comment '状态变更原因',
  modify column `created_at` datetime not null comment '创建时间';

alter table `intent_request` comment = '意图请求表，记录用户提交给AI解析的原始请求';
alter table `intent_request`
  modify column `id` bigint not null auto_increment comment '意图请求ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `user_id` bigint not null comment '提交用户ID',
  modify column `source_type` varchar(32) not null comment '来源类型，如TEXT、WEB、DOUYIN',
  modify column `input_text` text comment '用户输入文本',
  modify column `source_url` varchar(1000) comment '来源链接',
  modify column `image_url` varchar(255) comment '图片URL',
  modify column `note` varchar(500) comment '用户备注',
  modify column `status` varchar(32) not null comment '请求处理状态',
  modify column `ai_task_id` bigint comment '关联AI任务ID',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间';

alter table `ai_task` comment = 'AI任务表，记录AI解析、推荐和菜单规划等任务';
alter table `ai_task`
  modify column `id` bigint not null auto_increment comment 'AI任务ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `user_id` bigint not null comment '发起用户ID',
  modify column `task_type` varchar(32) not null comment '任务类型',
  modify column `source_type` varchar(32) not null comment '输入来源类型',
  modify column `input_text` text comment '输入文本',
  modify column `source_url` varchar(1000) comment '来源链接',
  modify column `image_url` varchar(255) comment '图片URL',
  modify column `status` varchar(32) not null comment '任务状态',
  modify column `result_summary` varchar(1000) comment 'AI结果摘要',
  modify column `error_code` varchar(64) comment '错误编码',
  modify column `error_message` varchar(500) comment '错误信息',
  modify column `retry_count` int not null default 0 comment '重试次数',
  modify column `model_name` varchar(128) comment '使用的AI模型名称',
  modify column `prompt_tokens` int comment '提示词token数量',
  modify column `completion_tokens` int comment '输出token数量',
  modify column `started_at` datetime comment '任务开始时间',
  modify column `finished_at` datetime comment '任务结束时间',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间';

alter table `ai_source_content` comment = 'AI来源内容表，保存链接解析得到的网页或平台内容';
alter table `ai_source_content`
  modify column `id` bigint not null auto_increment comment '来源内容ID',
  modify column `ai_task_id` bigint not null comment 'AI任务ID',
  modify column `resolved_url` varchar(1000) comment '解析后的最终URL',
  modify column `title` varchar(255) comment '来源标题',
  modify column `description` varchar(1000) comment '来源描述',
  modify column `content_text` mediumtext comment '提取出的正文内容',
  modify column `cover_url` varchar(1000) comment '封面图片URL',
  modify column `raw_metadata_json` json comment '原始元数据JSON';

alter table `ai_extracted_dish` comment = 'AI提取菜品表，保存AI从文本或链接中识别出的待审核菜品';
alter table `ai_extracted_dish`
  modify column `id` bigint not null auto_increment comment 'AI提取菜品ID',
  modify column `ai_task_id` bigint not null comment 'AI任务ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `name` varchar(128) not null comment '菜品名称',
  modify column `aliases` varchar(255) comment '菜品别名',
  modify column `category_name` varchar(64) comment 'AI识别的分类名称',
  modify column `tags_json` json comment 'AI识别的标签JSON',
  modify column `taste` varchar(128) comment '口味描述',
  modify column `meal_types_json` json comment '适用餐别JSON',
  modify column `difficulty` varchar(16) comment '制作难度',
  modify column `estimated_minutes` int comment '预计制作分钟数',
  modify column `ingredients_json` json comment '食材列表JSON',
  modify column `instructions` text comment '制作步骤',
  modify column `recommendation_reason` varchar(1000) comment '推荐或识别原因',
  modify column `confidence` decimal(5,4) comment 'AI置信度',
  modify column `match_dish_id` bigint comment '匹配到的已有菜品ID',
  modify column `match_score` decimal(5,4) comment '与已有菜品的匹配分数',
  modify column `review_status` varchar(32) not null comment '审核状态',
  modify column `converted_dish_id` bigint comment '审核通过后转换成的菜品ID',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间';

alter table `ai_recommendation` comment = 'AI推荐结果表，保存一次推荐任务产生的候选菜品';
alter table `ai_recommendation`
  modify column `id` bigint not null auto_increment comment 'AI推荐ID',
  modify column `ai_task_id` bigint comment 'AI任务ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `user_id` bigint not null comment '请求用户ID',
  modify column `prompt` varchar(1000) not null comment '推荐提示词',
  modify column `dish_id` bigint comment '推荐的已有菜品ID',
  modify column `extracted_dish_id` bigint comment '推荐的AI提取菜品ID',
  modify column `title` varchar(128) not null comment '推荐标题',
  modify column `reason` varchar(1000) not null comment '推荐理由',
  modify column `score` decimal(5,4) comment '推荐分数',
  modify column `created_at` datetime not null comment '创建时间';

alter table `shopping_list` comment = '采购清单表，按餐次生成家庭采购清单';
alter table `shopping_list`
  modify column `id` bigint not null auto_increment comment '采购清单ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `meal_session_id` bigint not null comment '餐次ID',
  modify column `title` varchar(128) not null comment '采购清单标题',
  modify column `status` varchar(16) not null comment '清单状态',
  modify column `generated_by_ai` tinyint not null default 0 comment '是否由AI生成，1是，0否',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间';

alter table `shopping_list_item` comment = '采购清单明细表，记录需要购买的食材';
alter table `shopping_list_item`
  modify column `id` bigint not null auto_increment comment '采购清单明细ID',
  modify column `shopping_list_id` bigint not null comment '采购清单ID',
  modify column `name` varchar(128) not null comment '食材名称',
  modify column `amount` decimal(10,2) comment '采购数量',
  modify column `unit` varchar(32) comment '采购单位',
  modify column `category` varchar(64) comment '食材分类',
  modify column `checked` tinyint not null default 0 comment '是否已勾选采购，1是，0否',
  modify column `source` varchar(32) not null comment '来源类型，如DISH或MANUAL',
  modify column `source_dish_ids` varchar(255) comment '来源菜品ID列表',
  modify column `note` varchar(255) comment '明细备注',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间';

alter table `system_config` comment = '系统配置表，按家庭保存可配置项';
alter table `system_config`
  modify column `id` bigint not null auto_increment comment '配置ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `config_key` varchar(128) not null comment '配置键',
  modify column `config_value` text comment '配置值',
  modify column `value_type` varchar(16) not null comment '配置值类型',
  modify column `encrypted` tinyint not null default 0 comment '是否加密存储，1是，0否',
  modify column `updated_at` datetime not null comment '更新时间';

alter table `ai_call_log` comment = 'AI调用日志表，记录AI服务调用结果和token用量';
alter table `ai_call_log`
  modify column `id` bigint not null auto_increment comment 'AI调用日志ID',
  modify column `family_id` bigint not null comment '家庭ID',
  modify column `user_id` bigint comment '调用用户ID',
  modify column `ai_task_id` bigint comment 'AI任务ID',
  modify column `provider` varchar(64) not null comment 'AI服务提供商',
  modify column `model_name` varchar(128) comment 'AI模型名称',
  modify column `prompt_tokens` int comment '提示词token数量',
  modify column `completion_tokens` int comment '输出token数量',
  modify column `success` tinyint not null comment '调用是否成功，1成功，0失败',
  modify column `error_code` varchar(64) comment '错误编码',
  modify column `created_at` datetime not null comment '创建时间';
