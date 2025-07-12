package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺管理
 */
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {
    public static final String KEY = "SHOP_STATUS";

    @Autowired
    RedisTemplate redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("设置店铺状态")
    public Result setShopStatus(@PathVariable Integer status){
        log.info("设置店铺状态: {}",status);
        redisTemplate.opsForValue().set(ShopController.KEY,status);

        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("管理端获取店铺状态")
    public Result<Integer> getShopStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(ShopController.KEY);
        log.info("获取到的状态: {}",status == 1 ? "营业中" : "打烊中");

        return Result.success(status);
    }
}

