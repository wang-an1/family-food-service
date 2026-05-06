create table QRTZ_JOB_DETAILS(
SCHED_NAME varchar(120) not null,
JOB_NAME varchar(190) not null,
JOB_GROUP varchar(190) not null,
DESCRIPTION varchar(250) null,
JOB_CLASS_NAME varchar(250) not null,
IS_DURABLE varchar(1) not null,
IS_NONCONCURRENT varchar(1) not null,
IS_UPDATE_DATA varchar(1) not null,
REQUESTS_RECOVERY varchar(1) not null,
JOB_DATA blob null,
primary key (SCHED_NAME, JOB_NAME, JOB_GROUP))
engine=InnoDB;

create table QRTZ_TRIGGERS (
SCHED_NAME varchar(120) not null,
TRIGGER_NAME varchar(190) not null,
TRIGGER_GROUP varchar(190) not null,
JOB_NAME varchar(190) not null,
JOB_GROUP varchar(190) not null,
DESCRIPTION varchar(250) null,
NEXT_FIRE_TIME bigint(13) null,
PREV_FIRE_TIME bigint(13) null,
PRIORITY integer null,
TRIGGER_STATE varchar(16) not null,
TRIGGER_TYPE varchar(8) not null,
START_TIME bigint(13) not null,
END_TIME bigint(13) null,
CALENDAR_NAME varchar(190) null,
MISFIRE_INSTR smallint(2) null,
JOB_DATA blob null,
primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
foreign key (SCHED_NAME, JOB_NAME, JOB_GROUP)
references QRTZ_JOB_DETAILS(SCHED_NAME, JOB_NAME, JOB_GROUP))
engine=InnoDB;

create table QRTZ_SIMPLE_TRIGGERS (
SCHED_NAME varchar(120) not null,
TRIGGER_NAME varchar(190) not null,
TRIGGER_GROUP varchar(190) not null,
REPEAT_COUNT bigint(7) not null,
REPEAT_INTERVAL bigint(12) not null,
TIMES_TRIGGERED bigint(10) not null,
primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
references QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP))
engine=InnoDB;

create table QRTZ_CRON_TRIGGERS (
SCHED_NAME varchar(120) not null,
TRIGGER_NAME varchar(190) not null,
TRIGGER_GROUP varchar(190) not null,
CRON_EXPRESSION varchar(120) not null,
TIME_ZONE_ID varchar(80),
primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
references QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP))
engine=InnoDB;

create table QRTZ_SIMPROP_TRIGGERS (
SCHED_NAME varchar(120) not null,
TRIGGER_NAME varchar(190) not null,
TRIGGER_GROUP varchar(190) not null,
STR_PROP_1 varchar(512) null,
STR_PROP_2 varchar(512) null,
STR_PROP_3 varchar(512) null,
INT_PROP_1 int null,
INT_PROP_2 int null,
LONG_PROP_1 bigint null,
LONG_PROP_2 bigint null,
DEC_PROP_1 numeric(13,4) null,
DEC_PROP_2 numeric(13,4) null,
BOOL_PROP_1 varchar(1) null,
BOOL_PROP_2 varchar(1) null,
primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
references QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP))
engine=InnoDB;

create table QRTZ_BLOB_TRIGGERS (
SCHED_NAME varchar(120) not null,
TRIGGER_NAME varchar(190) not null,
TRIGGER_GROUP varchar(190) not null,
BLOB_DATA blob null,
primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
index (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
references QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP))
engine=InnoDB;

create table QRTZ_CALENDARS (
SCHED_NAME varchar(120) not null,
CALENDAR_NAME varchar(190) not null,
CALENDAR blob not null,
primary key (SCHED_NAME, CALENDAR_NAME))
engine=InnoDB;

create table QRTZ_PAUSED_TRIGGER_GRPS (
SCHED_NAME varchar(120) not null,
TRIGGER_GROUP varchar(190) not null,
primary key (SCHED_NAME, TRIGGER_GROUP))
engine=InnoDB;

create table QRTZ_FIRED_TRIGGERS (
SCHED_NAME varchar(120) not null,
ENTRY_ID varchar(95) not null,
TRIGGER_NAME varchar(190) not null,
TRIGGER_GROUP varchar(190) not null,
INSTANCE_NAME varchar(190) not null,
FIRED_TIME bigint(13) not null,
SCHED_TIME bigint(13) not null,
PRIORITY integer not null,
STATE varchar(16) not null,
JOB_NAME varchar(190) null,
JOB_GROUP varchar(190) null,
IS_NONCONCURRENT varchar(1) null,
REQUESTS_RECOVERY varchar(1) null,
primary key (SCHED_NAME, ENTRY_ID))
engine=InnoDB;

create table QRTZ_SCHEDULER_STATE (
SCHED_NAME varchar(120) not null,
INSTANCE_NAME varchar(190) not null,
LAST_CHECKIN_TIME bigint(13) not null,
CHECKIN_INTERVAL bigint(13) not null,
primary key (SCHED_NAME, INSTANCE_NAME))
engine=InnoDB;

create table QRTZ_LOCKS (
SCHED_NAME varchar(120) not null,
LOCK_NAME varchar(40) not null,
primary key (SCHED_NAME, LOCK_NAME))
engine=InnoDB;

create index IDX_QRTZ_J_REQ_RECOVERY on QRTZ_JOB_DETAILS(SCHED_NAME, REQUESTS_RECOVERY);
create index IDX_QRTZ_J_GRP on QRTZ_JOB_DETAILS(SCHED_NAME, JOB_GROUP);
create index IDX_QRTZ_T_J on QRTZ_TRIGGERS(SCHED_NAME, JOB_NAME, JOB_GROUP);
create index IDX_QRTZ_T_JG on QRTZ_TRIGGERS(SCHED_NAME, JOB_GROUP);
create index IDX_QRTZ_T_C on QRTZ_TRIGGERS(SCHED_NAME, CALENDAR_NAME);
create index IDX_QRTZ_T_G on QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_GROUP);
create index IDX_QRTZ_T_STATE on QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_STATE);
create index IDX_QRTZ_T_N_STATE on QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, TRIGGER_STATE);
create index IDX_QRTZ_T_N_G_STATE on QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_GROUP, TRIGGER_STATE);
create index IDX_QRTZ_T_NEXT_FIRE_TIME on QRTZ_TRIGGERS(SCHED_NAME, NEXT_FIRE_TIME);
create index IDX_QRTZ_T_NFT_ST on QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_STATE, NEXT_FIRE_TIME);
create index IDX_QRTZ_T_NFT_MISFIRE on QRTZ_TRIGGERS(SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME);
create index IDX_QRTZ_T_NFT_ST_MISFIRE on QRTZ_TRIGGERS(SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_STATE);
create index IDX_QRTZ_T_NFT_ST_MISFIRE_GRP on QRTZ_TRIGGERS(SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_GROUP, TRIGGER_STATE);
create index IDX_QRTZ_FT_TRIG_INST_NAME on QRTZ_FIRED_TRIGGERS(SCHED_NAME, INSTANCE_NAME);
create index IDX_QRTZ_FT_INST_JOB_REQ_RCVRY on QRTZ_FIRED_TRIGGERS(SCHED_NAME, INSTANCE_NAME, REQUESTS_RECOVERY);
create index IDX_QRTZ_FT_J_G on QRTZ_FIRED_TRIGGERS(SCHED_NAME, JOB_NAME, JOB_GROUP);
create index IDX_QRTZ_FT_JG on QRTZ_FIRED_TRIGGERS(SCHED_NAME, JOB_GROUP);
create index IDX_QRTZ_FT_T_G on QRTZ_FIRED_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
create index IDX_QRTZ_FT_TG on QRTZ_FIRED_TRIGGERS(SCHED_NAME, TRIGGER_GROUP);

create table scheduled_task (
  id bigint primary key auto_increment,
  name varchar(128) not null,
  task_type varchar(64) not null,
  status varchar(16) not null,
  schedule_mode varchar(16) not null,
  cron_expression varchar(120),
  schedule_config_json json,
  parameters_json json,
  job_name varchar(190) not null,
  job_group varchar(190) not null,
  trigger_name varchar(190) not null,
  trigger_group varchar(190) not null,
  description varchar(500),
  created_at datetime not null,
  updated_at datetime not null,
  created_by bigint,
  updated_by bigint,
  constraint uk_scheduled_task_job unique (job_name, job_group),
  constraint uk_scheduled_task_trigger unique (trigger_name, trigger_group)
);
create index idx_scheduled_task_status on scheduled_task(status, task_type);
create index idx_scheduled_task_updated on scheduled_task(updated_at);

create table scheduled_task_run_log (
  id bigint primary key auto_increment,
  scheduled_task_id bigint not null,
  trigger_type varchar(16) not null,
  status varchar(16) not null,
  started_at datetime not null,
  finished_at datetime,
  duration_millis bigint,
  error_message varchar(1000),
  created_at datetime not null
);
create index idx_scheduled_task_run_task_time on scheduled_task_run_log(scheduled_task_id, created_at desc, id desc);
create index idx_scheduled_task_run_status on scheduled_task_run_log(status, created_at desc);
