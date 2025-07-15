package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

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

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 根据map查询用户数
     * @param map
     * @return {@link Integer }
     */
    Integer countByMap(Map map);
}
