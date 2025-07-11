package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    DishService dishService;

    /**
     * 添加菜品
     * @param dishDTO
     * @return {@link Result }
     */
    @PostMapping
    @ApiOperation("添加菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        dishService.save(dishDTO);

        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return {@link Result }<{@link PageResult }>
     */
    @GetMapping("page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询: {}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return {@link Result }
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("批量删除菜品: {}",ids);
        dishService.deleteBatch(ids);

        return Result.success();
    }

    /**
     * 查询带口味的菜品
     * @param id
     * @return {@link Result }<{@link DishVO }>
     */
    @GetMapping("/{id}")
    @ApiOperation("查询带口味的菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("查询带口味的菜品: {}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);

        return Result.success(dishVO);
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return {@link Result }
     */
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息: {}",dishDTO);
        dishService.update(dishDTO);

        return Result.success();
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     * @return {@link Result }
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result enableSell(@PathVariable Integer status,Long id){
        dishService.enableSell(status,id);

        return Result.success();
    }

    /**
     * 根据分类查询菜品
     * @param categoryId
     * @return {@link Result }<{@link DishVO }>
     */
    @GetMapping("/list")
    public Result<List<Dish>> getDishesByCategoryId(Long categoryId){
        log.info("根据分类查询菜品: {}",categoryId);
        List<Dish> dishes = dishService.list(categoryId);

        return Result.success(dishes);
    }
}
