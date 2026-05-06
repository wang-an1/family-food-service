package com.familyfood.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.system.entity.ScheduledTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduledTaskMapper extends BaseMapper<ScheduledTask> {
}
