package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;

    @Autowired
    DishFlavorMapper dishFlavorMapper;

    @Autowired
    SetmealDishMapper setmealDishMapper;

    /**
     * 增加菜品
     * @param dishDTO
     */
    @Override
    @Transactional
    public void save(DishDTO dishDTO) {
        //获取实体类
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //插入菜品表
        dishMapper.insert(dish);

        Long dishId = dish.getId();
        //插入口味表
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if(flavors != null && !flavors.isEmpty()){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });

            dishFlavorMapper.insert(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return {@link PageResult }
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //分页开始
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        //获取Page
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        //返回结果
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //存在在售的无法删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);

            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //存在套餐绑定的无法删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);

        if(setmealIds != null && !setmealIds.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品、口味
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 通过id获得DishVO
     * @param id
     * @return {@link DishVO }
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {

        return dishMapper.getByIdWithFlavor(id);
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        //修改基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.update(dish);
        //删除当前口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //插入修改后的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });

            dishFlavorMapper.insert(flavors);
        }
    }

    /**
     * 根据分类查询菜品
     * @param categoryId
     * @return {@link List }<{@link DishVO }>
     */
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();

        return dishMapper.list(dish);
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    @Override
    public void enableSell(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();

        dishMapper.update(dish);
    }
}
