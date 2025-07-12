package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 增加菜品
     * @param dishDTO
     */
    void save(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return {@link PageResult }
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 通过id获得DishVO
     * @param id
     * @return {@link DishVO }
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    void update(DishDTO dishDTO);

    /**
     *菜品起售停售
     * @param status
     * @param id
     */
    void enableSell(Integer status,Long id);

    /**
     * 根据分类查询菜品
     * @param categoryId
     * @return {@link List }<{@link DishVO }>
     */
    List<Dish> list(Long categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
