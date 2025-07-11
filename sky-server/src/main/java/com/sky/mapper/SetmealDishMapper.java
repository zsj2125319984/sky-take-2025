package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据dishIds查询SetmealIdBs
     * @param dishIds
     * @return {@link List }<{@link Long }>
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量保存套餐菜品关联
     * @param setmealDishes
     */
    void saveBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据setmealIds删除
     * @param setmealIds
     */
    void deleteBySetmealIds(List<Long> setmealIds);

    /**
     * 据setmealId查询
     * @param setmealId
     * @return {@link SetmealDish }
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);

    /**
     * 根据setmealId删除
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{id}")
    void deleteBySetmealId(Long setmealId);
}
