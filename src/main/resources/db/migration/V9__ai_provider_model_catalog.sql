create table ai_provider_catalog (
  id bigint primary key auto_increment,
  code varchar(64) not null,
  display_name varchar(128) not null,
  call_type varchar(64) not null,
  base_url varchar(500),
  status varchar(16) not null,
  sort_order int not null default 0,
  created_at datetime not null,
  updated_at datetime not null,
  constraint uk_ai_provider_catalog_code unique (code)
);
create index idx_ai_provider_catalog_status_sort on ai_provider_catalog(status, sort_order, id);

create table ai_model_catalog (
  id bigint primary key auto_increment,
  provider_id bigint not null,
  model_name varchar(128) not null,
  display_name varchar(128) not null,
  default_model tinyint not null default 0,
  status varchar(16) not null,
  sort_order int not null default 0,
  created_at datetime not null,
  updated_at datetime not null,
  constraint uk_ai_model_catalog_provider_model unique (provider_id, model_name),
  constraint fk_ai_model_catalog_provider foreign key (provider_id) references ai_provider_catalog(id)
);
create index idx_ai_model_catalog_provider_status_sort on ai_model_catalog(provider_id, status, sort_order, id);

insert into ai_provider_catalog (code, display_name, call_type, base_url, status, sort_order, created_at, updated_at)
values
  ('deepseek', 'DeepSeek', 'OPENAI_CHAT_COMPLETIONS', 'https://api.deepseek.com', 'ACTIVE', 10, current_timestamp, current_timestamp),
  ('openai-compatible', 'OpenAI-compatible', 'OPENAI_CHAT_COMPLETIONS', 'https://api.openai.com/v1', 'ACTIVE', 20, current_timestamp, current_timestamp),
  ('mock', 'Mock Provider', 'MOCK', null, 'ACTIVE', 90, current_timestamp, current_timestamp);

insert into ai_model_catalog (provider_id, model_name, display_name, default_model, status, sort_order, created_at, updated_at)
select id, 'deepseek-v4-pro', 'DeepSeek V4 Pro', 1, 'ACTIVE', 10, current_timestamp, current_timestamp
from ai_provider_catalog
where code = 'deepseek';

insert into ai_model_catalog (provider_id, model_name, display_name, default_model, status, sort_order, created_at, updated_at)
select id, 'deepseek-v4-flash', 'DeepSeek V4 Flash', 0, 'ACTIVE', 20, current_timestamp, current_timestamp
from ai_provider_catalog
where code = 'deepseek';

insert into ai_model_catalog (provider_id, model_name, display_name, default_model, status, sort_order, created_at, updated_at)
select id, 'gpt-5.5', 'GPT-5.5', 1, 'ACTIVE', 10, current_timestamp, current_timestamp
from ai_provider_catalog
where code = 'openai-compatible';

insert into ai_model_catalog (provider_id, model_name, display_name, default_model, status, sort_order, created_at, updated_at)
select id, 'gpt-5.4', 'GPT-5.4', 0, 'ACTIVE', 20, current_timestamp, current_timestamp
from ai_provider_catalog
where code = 'openai-compatible';

update system_config
set config_value = 'deepseek',
    updated_at = current_timestamp
where config_key = 'ai.provider'
  and config_value = 'deepseek-v4';

update system_config
set config_value = 'deepseek-v4-pro',
    updated_at = current_timestamp
where config_key = 'ai.chat_model'
  and config_value = 'deepseek-v4';
