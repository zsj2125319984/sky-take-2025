package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;

    @Autowired
    DishFlavorMapper dishFlavorMapper;

    /**
     * 增加菜品
     * @param dishDTO
     */
    @Override
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
}
