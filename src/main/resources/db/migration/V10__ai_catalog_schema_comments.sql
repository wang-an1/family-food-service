alter table `ai_provider_catalog` comment = 'AI供应商目录表，全系统共享可选供应商';
alter table `ai_provider_catalog`
  modify column `id` bigint not null auto_increment comment '供应商目录ID',
  modify column `code` varchar(64) not null comment '供应商编码，创建后不可修改，用于系统配置引用',
  modify column `display_name` varchar(128) not null comment '供应商展示名称',
  modify column `call_type` varchar(64) not null comment '调用类型，如OPENAI_CHAT_COMPLETIONS或MOCK',
  modify column `base_url` varchar(500) comment 'API Base URL，Mock供应商为空',
  modify column `status` varchar(16) not null comment '供应商状态，ACTIVE启用，INACTIVE停用',
  modify column `sort_order` int not null default 0 comment '排序值，越小越靠前',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间';

alter table `ai_model_catalog` comment = 'AI模型目录表，维护各供应商下可选模型';
alter table `ai_model_catalog`
  modify column `id` bigint not null auto_increment comment '模型目录ID',
  modify column `provider_id` bigint not null comment '所属AI供应商目录ID',
  modify column `model_name` varchar(128) not null comment '模型名称，作为调用请求中的model值',
  modify column `display_name` varchar(128) not null comment '模型展示名称',
  modify column `default_model` tinyint not null default 0 comment '是否默认模型，1是，0否',
  modify column `status` varchar(16) not null comment '模型状态，ACTIVE启用，INACTIVE停用',
  modify column `sort_order` int not null default 0 comment '排序值，越小越靠前',
  modify column `created_at` datetime not null comment '创建时间',
  modify column `updated_at` datetime not null comment '更新时间';
