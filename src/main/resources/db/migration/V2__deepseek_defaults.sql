update system_config
set config_value = 'deepseek',
    updated_at = current_timestamp
where config_key = 'ai.provider'
  and config_value in ('mock', 'openai-compatible');

insert into system_config (family_id, config_key, config_value, value_type, encrypted, updated_at)
select f.id, 'ai.base_url', 'https://api.deepseek.com', 'STRING', 0, current_timestamp
from family f
where not exists (
  select 1
  from system_config c
  where c.family_id = f.id
    and c.config_key = 'ai.base_url'
);

insert into system_config (family_id, config_key, config_value, value_type, encrypted, updated_at)
select f.id, 'ai.chat_model', 'deepseek-v4', 'STRING', 0, current_timestamp
from family f
where not exists (
  select 1
  from system_config c
  where c.family_id = f.id
    and c.config_key = 'ai.chat_model'
);
