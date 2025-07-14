package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理
 * @author Sicecream
 * @date 2025/07/14
 */
@RestController
@RequestMapping("/admin/order")
@Api(tags = "订单管理接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     * @return {@link Result }<{@link PageResult }>
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索");
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 各状态订单数量统计
     * @return {@link Result }<{@link OrderStatisticsVO }>
     */
    @GetMapping("/statistics")
    @ApiOperation("各状态订单数量统计")
    public Result<OrderStatisticsVO> statistics(){
        log.info("各状态订单数量统计: ");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();

        return Result.success(orderStatisticsVO);
    }

    /**
     * 接单
     * @return {@link Result }
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接单: {}",ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);

        return Result.success();
    }

    /**
     * 拒单
     * @return {@link Result }
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        log.info("拒单: {}",ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);

        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return {@link Result }
     */
    @PutMapping("/cancel")
    @ApiOperation("商家取消订单")
    public Result cancelAdmin(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        log.info("商家取消订单: {}",ordersCancelDTO);
        orderService.cancelAdmin(ordersCancelDTO);

        return Result.success();
    }

    /**
     * 派送订单
     * @param id
     * @return {@link Result }
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id){
        log.info("派送订单: {}",id);
        orderService.delivery(id);

        return Result.success();
    }

    /**
     * 完成订单
     * @param id
     * @return {@link Result }
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id){
        log.info("完成订单: {}",id);
        orderService.complete(id);

        return Result.success();
    }

    /**
     * 查看订单详细
     * @param id
     * @return {@link Result }<{@link OrderVO }>
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查看订单详细")
    public Result<OrderVO> detail(@PathVariable Long id){
        log.info("查看订单详细: {}",id);
        OrderVO orderVO = orderService.orderInfo(id);

        return Result.success(orderVO);
    }
}
