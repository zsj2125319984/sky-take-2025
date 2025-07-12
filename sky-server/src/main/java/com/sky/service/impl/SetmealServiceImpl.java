package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    SetmealMapper setmealMapper;

    @Autowired
    SetmealDishMapper setmealDishMapper;

    @Autowired
    DishMapper dishMapper;

    /**
     * 根据id查询套餐
     * @param id
     * @return {@link SetmealVO }
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 启用停用套餐
     * @param status
     */
    @Override
    public void enableSetmeal(Integer status,Long id) {
        //起售时，若有菜品停售则无法起售
        if(status == StatusConstant.ENABLE){
            List<Dish> dishes = dishMapper.getBySetmealId(id);

            for (Dish dish : dishes) {
                if(dish.getStatus() == StatusConstant.DISABLE){
                    throw new SetmealEnableFailedException
                            (MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();

        setmealMapper.update(setmeal);
    }

    /**
     * SetmealDish
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //创建实体对象
        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(setmealDTO,setmeal);
        //修改套餐表
        setmealMapper.update(setmeal);
        //删除套餐菜品关联
        Long setmealId = setmealDTO.getId();

        setmealDishMapper.deleteBySetmealId(setmealId);
        //插入新的关联
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        setmealDishMapper.saveBatch(setmealDishes);
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //起售中的无法删除
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);

            if(setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //删除套餐
        setmealMapper.deleteByIds(ids);
        //删除套餐菜品关联
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return {@link PageResult }
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        //创建实体类
        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(setmealDTO,setmeal);
        //插入套餐表
        setmealMapper.save(setmeal);
        //获取生成的套餐id
        Long setmealId = setmeal.getId();
        //获取并设置套餐菜品关联
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        setmealDishes.forEach(setmealDish ->
                setmealDish.setSetmealId(setmealId));

        //批量插入关联
        setmealDishMapper.saveBatch(setmealDishes);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
