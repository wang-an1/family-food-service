package com.familyfood.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.familyfood.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
