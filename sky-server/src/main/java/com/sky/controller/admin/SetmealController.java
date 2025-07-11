package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @return {@link Result }
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        setmealService.save(setmealDTO);

        return Result.success();
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return {@link Result }<{@link PageResult }>
     */
    @GetMapping("/page")
    @ApiOperation(("套餐分页查询"))
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询: {}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return {@link Result }
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("批量删除套餐: {}",ids);
        setmealService.deleteBatch(ids);

        return Result.success();
    }

    /**
     * 修改套餐
     * @return {@link Result }
     */
    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐: {}",setmealDTO);
        setmealService.update(setmealDTO);

        return Result.success();
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return {@link Result }<{@link SetmealVO }>
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getByIdWithDish(@PathVariable Long id){
        log.info("根据id查询套餐: {}",id);
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);

        return Result.success(setmealVO);
    }

    /**
     * 启用停用套餐
     * @param status
     * @return {@link Result }
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用停用套餐")
    public Result enableSetmeal(@PathVariable Integer status,Long id){
        log.info("启用停用套餐: {},{}",id,status);
        setmealService.enableSetmeal(status,id);

        return Result.success();
    }
}