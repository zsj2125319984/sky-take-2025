package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 插入口味
     * @param flavors
     */
    void insert(List<DishFlavor> flavors);

    /**
     * 根据菜品id删除
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 根据菜品ids删除
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);
}
