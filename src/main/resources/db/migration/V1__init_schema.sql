create table family (
  id bigint primary key auto_increment,
  name varchar(64) not null,
  invite_code varchar(32) unique,
  status varchar(16) not null,
  created_at datetime not null,
  updated_at datetime not null
);

create table user (
  id bigint primary key auto_increment,
  username varchar(64) not null unique,
  password_hash varchar(128) not null,
  nickname varchar(64) not null,
  avatar_url varchar(255),
  phone varchar(32),
  status varchar(16) not null,
  created_at datetime not null,
  updated_at datetime not null
);

create table family_member (
  id bigint primary key auto_increment,
  family_id bigint not null,
  user_id bigint not null,
  role varchar(16) not null,
  display_name varchar(64),
  status varchar(16) not null,
  joined_at datetime not null,
  constraint uk_family_user unique (family_id, user_id)
);
create index idx_family_member_family on family_member(family_id);

create table dish_category (
  id bigint primary key auto_increment,
  family_id bigint not null,
  name varchar(64) not null,
  sort_order int not null default 0,
  created_at datetime not null,
  updated_at datetime not null,
  constraint uk_dish_category_name unique (family_id, name)
);

create table dish (
  id bigint primary key auto_increment,
  family_id bigint not null,
  category_id bigint,
  name varchar(128) not null,
  aliases varchar(255),
  description varchar(1000),
  image_url varchar(255),
  taste varchar(128),
  meal_types varchar(128),
  difficulty varchar(16),
  estimated_minutes int,
  default_servings decimal(6,2) not null default 1,
  instructions text,
  source_type varchar(32) not null,
  source_url varchar(1000),
  status varchar(16) not null,
  created_at datetime not null,
  updated_at datetime not null,
  created_by bigint,
  updated_by bigint,
  deleted tinyint not null default 0
);
create index idx_dish_family_status on dish(family_id, status);
create index idx_dish_category on dish(category_id);
create index idx_dish_name on dish(family_id, name);

create table dish_tag (
  id bigint primary key auto_increment,
  family_id bigint not null,
  name varchar(64) not null,
  color varchar(16),
  constraint uk_dish_tag_name unique (family_id, name)
);

create table dish_tag_relation (
  id bigint primary key auto_increment,
  dish_id bigint not null,
  tag_id bigint not null,
  constraint uk_dish_tag_relation unique (dish_id, tag_id)
);

create table dish_ingredient (
  id bigint primary key auto_increment,
  dish_id bigint not null,
  name varchar(128) not null,
  amount decimal(10,2),
  unit varchar(32),
  category varchar(64),
  required tinyint not null default 1,
  note varchar(255)
);
create index idx_ingredient_dish on dish_ingredient(dish_id);

create table meal_session (
  id bigint primary key auto_increment,
  family_id bigint not null,
  title varchar(128) not null,
  meal_type varchar(16) not null,
  meal_date date not null,
  expected_time datetime,
  status varchar(16) not null,
  confirm_required tinyint not null,
  created_at datetime not null,
  updated_at datetime not null,
  created_by bigint
);
create index idx_meal_session_family_date on meal_session(family_id, meal_date);
create index idx_meal_session_status on meal_session(family_id, status);

create table personal_order (
  id bigint primary key auto_increment,
  family_id bigint not null,
  meal_session_id bigint not null,
  user_id bigint not null,
  status varchar(32) not null,
  note varchar(500),
  avoidances varchar(500),
  expected_time datetime,
  submitted_at datetime,
  confirmed_at datetime,
  confirmed_by bigint,
  created_at datetime not null,
  updated_at datetime not null,
  deleted tinyint not null default 0
);
create index idx_order_session on personal_order(meal_session_id);
create index idx_order_user on personal_order(user_id, status);
create index idx_order_family_status on personal_order(family_id, status);

create table order_item (
  id bigint primary key auto_increment,
  order_id bigint not null,
  dish_id bigint not null,
  dish_name_snapshot varchar(128) not null,
  quantity decimal(8,2) not null,
  unit varchar(32) not null default '份',
  note varchar(255)
);
create index idx_order_item_order on order_item(order_id);
create index idx_order_item_dish on order_item(dish_id);

create table order_status_log (
  id bigint primary key auto_increment,
  order_id bigint not null,
  from_status varchar(32),
  to_status varchar(32) not null,
  operator_id bigint,
  reason varchar(255),
  created_at datetime not null
);

create table intent_request (
  id bigint primary key auto_increment,
  family_id bigint not null,
  user_id bigint not null,
  source_type varchar(32) not null,
  input_text text,
  source_url varchar(1000),
  image_url varchar(255),
  note varchar(500),
  status varchar(32) not null,
  ai_task_id bigint,
  created_at datetime not null,
  updated_at datetime not null
);

create table ai_task (
  id bigint primary key auto_increment,
  family_id bigint not null,
  user_id bigint not null,
  task_type varchar(32) not null,
  source_type varchar(32) not null,
  input_text text,
  source_url varchar(1000),
  image_url varchar(255),
  status varchar(32) not null,
  result_summary varchar(1000),
  error_code varchar(64),
  error_message varchar(500),
  retry_count int not null default 0,
  model_name varchar(128),
  prompt_tokens int,
  completion_tokens int,
  started_at datetime,
  finished_at datetime,
  created_at datetime not null,
  updated_at datetime not null
);
create index idx_ai_task_family_status on ai_task(family_id, status);
create index idx_ai_task_user on ai_task(user_id, created_at);

create table ai_source_content (
  id bigint primary key auto_increment,
  ai_task_id bigint not null,
  resolved_url varchar(1000),
  title varchar(255),
  description varchar(1000),
  content_text mediumtext,
  cover_url varchar(1000),
  raw_metadata_json json
);

create table ai_extracted_dish (
  id bigint primary key auto_increment,
  ai_task_id bigint not null,
  family_id bigint not null,
  name varchar(128) not null,
  aliases varchar(255),
  category_name varchar(64),
  tags_json json,
  taste varchar(128),
  meal_types_json json,
  difficulty varchar(16),
  estimated_minutes int,
  ingredients_json json,
  instructions text,
  recommendation_reason varchar(1000),
  confidence decimal(5,4),
  match_dish_id bigint,
  match_score decimal(5,4),
  review_status varchar(32) not null,
  converted_dish_id bigint,
  created_at datetime not null,
  updated_at datetime not null
);

create table ai_recommendation (
  id bigint primary key auto_increment,
  ai_task_id bigint,
  family_id bigint not null,
  user_id bigint not null,
  prompt varchar(1000) not null,
  dish_id bigint,
  extracted_dish_id bigint,
  title varchar(128) not null,
  reason varchar(1000) not null,
  score decimal(5,4),
  created_at datetime not null
);

create table shopping_list (
  id bigint primary key auto_increment,
  family_id bigint not null,
  meal_session_id bigint not null,
  title varchar(128) not null,
  status varchar(16) not null,
  generated_by_ai tinyint not null default 0,
  created_at datetime not null,
  updated_at datetime not null,
  constraint uk_shopping_list_session unique (meal_session_id)
);

create table shopping_list_item (
  id bigint primary key auto_increment,
  shopping_list_id bigint not null,
  name varchar(128) not null,
  amount decimal(10,2),
  unit varchar(32),
  category varchar(64),
  checked tinyint not null default 0,
  source varchar(32) not null,
  source_dish_ids varchar(255),
  note varchar(255),
  created_at datetime not null,
  updated_at datetime not null
);

create table system_config (
  id bigint primary key auto_increment,
  family_id bigint not null,
  config_key varchar(128) not null,
  config_value text,
  value_type varchar(16) not null,
  encrypted tinyint not null default 0,
  updated_at datetime not null,
  constraint uk_system_config_key unique (family_id, config_key)
);

create table ai_call_log (
  id bigint primary key auto_increment,
  family_id bigint not null,
  user_id bigint,
  ai_task_id bigint,
  provider varchar(64) not null,
  model_name varchar(128),
  prompt_tokens int,
  completion_tokens int,
  success tinyint not null,
  error_code varchar(64),
  created_at datetime not null
);
