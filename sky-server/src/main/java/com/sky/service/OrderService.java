package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return {@link OrderSubmitVO }
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
}