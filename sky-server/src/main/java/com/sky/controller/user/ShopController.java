package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "C端-店铺相关接口")
@Slf4j
public class ShopController {
    @Autowired
    RedisTemplate redisTemplate;

    @GetMapping("/status")
    @ApiOperation("C端获取店铺状态")
    public Result<Integer> getShopStatus(){
        Integer status = (Integer) redisTemplate
                .opsForValue()
                .get(com.sky.controller.admin.ShopController.KEY);
        log.info("获取到的状态: {}",status == 1 ? "营业中" : "打烊中");

        return Result.success();
    }
}