package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 通过openid查询用户
     * @param openid
     * @return {@link User }
     */
    User getByOpenId(String openid);

    /**
     * 插入用户
     * @param user
     */
    void save(User user);
}
