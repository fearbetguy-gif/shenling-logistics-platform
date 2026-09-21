package com.sl.express.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sl.express.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from `user` where phone = #{phone} limit 1")
    User findByPhone(String phone);
}
