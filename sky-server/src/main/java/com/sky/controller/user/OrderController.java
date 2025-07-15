package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单
 */
@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "C端-订单接口")
public class OrderController {

    @Autowired
    OrderService orderService;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return {@link Result }<{@link OrderSubmitVO }>
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单: {}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);

        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 历史订单分页查询
     * @param page
     * @param pageSize
     * @param status
     * @return {@link Result }<{@link PageResult }>
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单分页查询")
    public Result<PageResult> historyOrders(int page,int pageSize,Integer status){
        log.info("历史订单分页查询: {},{},{}",page,pageSize,status);
        PageResult pageResult = orderService.historyOrders(page,pageSize,status);

        return Result.success(pageResult);
    }

    /**
     * 查询订单详细
     * @param id
     * @return {@link Result }<{@link OrderVO }>
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详细")
    public Result<OrderVO> orderInfo(@PathVariable Long id){
        log.info("查询订单详细: {}",id);
        OrderVO orderVO = orderService.orderInfo(id);

        return Result.success(orderVO);
    }

    /**
     * 取消订单
     * @return {@link Result }
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id) throws Exception {
        log.info("取消订单: {}",id);
        orderService.cancel(id);

        return Result.success();
    }

    /**
     * 再来一单
     * @param id
     * @return {@link Result }
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result orderAgain(@PathVariable Long id){
        log.info("再来一单: {}",id);
        orderService.orderAgain(id);

        return Result.success();
    }

    /**
     * 用户催单
     * @return {@link Result }
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result reminder(@PathVariable Long id){
        log.info("用户催单: {}",id);
        orderService.reminder(id);

        return Result.success();
    }
}