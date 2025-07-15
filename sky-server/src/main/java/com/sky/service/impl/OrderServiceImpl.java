package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final String POS_URL = "https://api.map.baidu.com/geocoding/v3";

    private static final String ROUTE_URL = "https://api.map.baidu.com/directionlite/v1/driving";

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String baiduAk;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return {@link OrderSubmitVO }
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //未设置地址则抛出业务异常
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //检查用户收货地址是否超出范围
        checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() +
                addressBook.getDetail());

        //购物车为空则抛出业务异常
        Long currentId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list == null){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //订单表插入1条记录
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(currentId);

        orderMapper.save(orders);
        //明细表插入n条记录
        List<OrderDetail> orderDetailList = new ArrayList<>();

        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());

            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.saveBatch(orderDetailList);
        //清空用户购物车信息
        shoppingCartMapper.deleteByUserId(currentId);

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        return orderSubmitVO;
    }

    private void checkOutOfRange(String userAddress) {
        //获取店铺经纬度
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",baiduAk);

        String shopCoordinate = HttpClientUtil.doGet(POS_URL, map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);

        //判断获取状态是否正常
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        String shopPos = getPos(jsonObject);
        //获取用户经纬度
        map.put("address",userAddress);

        String userCoordinate = HttpClientUtil.doGet(POS_URL, map);

        jsonObject = JSON.parseObject(userCoordinate);

        //判断获取状态是否正常
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("用户地址解析失败");
        }

        String userPos = getPos(jsonObject);
        //通过路线规划获取大致距离，如果大致距离超过5000m则业务异常
        map.put("origin",shopPos);
        map.put("destination",userPos);
        map.put("steps_info","0");

        String json = HttpClientUtil.doGet(ROUTE_URL, map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }
        //数据解析
        JSONArray jsonArray = jsonObject
                .getJSONObject("result")
                .getJSONArray("routes");

        Integer distance = (Integer)((JSONObject)jsonArray.get(0)).get("distance");

        if(distance > 5000){
            throw new OrderBusinessException("超出配送范围");
        }
    }

    private String getPos(JSONObject jsonObject) {
        JSONObject location = jsonObject
                .getJSONObject("result")
                .getJSONObject("location");

        String lng = location.getString("lng");
        String lat = location.getString("lat");

        return lat + "," + lng;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 用户催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        //检查订单是否存在
        Orders orders = orderMapper.getById(id);

        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //给管理端发送消息
        Map map = new HashMap();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号: " + orders.getNumber());

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        //查询订单
        Orders ordersById = orderMapper.getById(id);
        //订单不存在或状态不为已派送则报错
        if(ordersById == null || !ordersById.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //更新订单
        Orders orders = new Orders();
        orders.setStatus(Orders.COMPLETED);
        orders.setId(ordersById.getId());

        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        //查询订单
        Orders ordersById = orderMapper.getById(id);
        //订单不存在或状态不为待派送则报错
        if(ordersById == null || !ordersById.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //更新订单
        Orders orders = new Orders();
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orders.setId(ordersById.getId());

        orderMapper.update(orders);
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancelAdmin(OrdersCancelDTO ordersCancelDTO) throws Exception {
        //查询订单
        Orders ordersById = orderMapper.getById(ordersCancelDTO.getId());
        //已支付则退款
        Integer payStatus = ordersById.getPayStatus();
        if (payStatus.equals(Orders.PAID)) {
            //用户已支付，需要退款
            String refund = weChatPayUtil.refund(
                    ordersById.getNumber(),
                    ordersById.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }
        //更新订单
        Orders orders = Orders.builder()
                .id(ordersById.getId())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        //根据id查询订单
        Orders ordersById = orderMapper.getById(ordersRejectionDTO.getId());
        //订单为空或状态不是待接单无法拒单
        if(ordersById == null || !ordersById.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //如果支付状态为已支付则退款
        Integer payStatus = ordersById.getPayStatus();
        if (payStatus.equals(Orders.PAID)) {
            //用户已支付，需要退款
            String refund = weChatPayUtil.refund(
                    ordersById.getNumber(),
                    ordersById.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }
        //更新订单信息
        Orders orders = Orders.builder()
                .id(ordersById.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        //将对应id的订单状态变为已接单
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 各状态订单数量统计
     * @return {@link OrderStatisticsVO }
     */
    @Override
    public OrderStatisticsVO statistics() {
        //查询各个状态订单数量
        Integer toBeConfirmed = orderMapper.getCountByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.getCountByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.getCountByStatus(Orders.DELIVERY_IN_PROGRESS);
        //返回结果
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return {@link PageResult }
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //开始分页查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        //查询出订单
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        //得到订单VO集合
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(),orderVOList);
    }

    /**
     * 根据分页结果获取订单VO集合
     * @param page
     * @return {@link List }<{@link OrderVO }>
     */
    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<OrderVO> orderVOList = new ArrayList<>();

        //获取菜品集合并将属性转化为订单VO
        List<Orders> ordersList = page.getResult();
        if(!CollectionUtils.isEmpty(ordersList)){
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);

                String orderDishes = getStringDishes(orders);
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }

        return orderVOList;
    }

    /**
     * 将订单明细内容转化为字符串
     * @param orders
     * @return {@link String }
     */
    private String getStringDishes(Orders orders) {
        //根据订单id查询明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        List<String> orderDishList = orderDetailList.stream()
                .map(x -> x.getName() + "*" + x.getNumber() + ";")
                .collect(Collectors.toList());

        return String.join("",orderDishList);
    }

    /**
     * @param id
     */
    @Override
    public void orderAgain(Long id) {
        //查询用户id
        Long currentId = BaseContext.getCurrentId();
        //根据id查询订单
        Orders orders = orderMapper.getById(id);
        //将根据订单id查询明细，将明细中的商品加入到购物车
        List<OrderDetail> detailList = orderDetailMapper.getByOrderId(id);

        List<ShoppingCart> shoppingCartList = detailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(currentId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());
        //购物车批量插入数据库
        shoppingCartMapper.saveBatch(shoppingCartList);
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancel(Long id) throws Exception {
        //查询订单
        Orders orderById = orderMapper.getById(id);
        //订单不存在或者订单状态不符合条件抛出业务异常
        if(orderById == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(orderById.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(orderById.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (orderById.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            weChatPayUtil.refund(
                    orderById.getNumber(), //商户订单号
                    orderById.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }
        //更新订单状态
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 查询订单详细
     * @param id
     * @return {@link OrderVO }
     */
    @Override
    public OrderVO orderInfo(Long id) {
        //根据id查询订单
        Orders orders = orderMapper.getById(id);
        //根据订单id查询明细
        List<OrderDetail> list = orderDetailMapper.getByOrderId(id);
        //返回结果
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(list);

        return orderVO;
    }

    /**
     * 历史订单分页查询
     * @param page
     * @param pageSize
     * @param status
     * @return {@link PageResult }
     */
    @Override
    public PageResult historyOrders(int page, int pageSize, Integer status) {
        //开始分页查询
        PageHelper.startPage(page,pageSize);
        //分页查询
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setPage(page);
        ordersPageQueryDTO.setPageSize(pageSize);
        ordersPageQueryDTO.setStatus(status);

        Page<Orders> pageQuery = orderMapper.pageQuery(ordersPageQueryDTO);
        //根据订单查询明细
        List<OrderVO> list = new ArrayList<>();

        if(pageQuery != null && pageQuery.getTotal() > 0){
            for (Orders orders : pageQuery) {
                Long orderId = orders.getId();

                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }
        //返回结果

        return new PageResult(pageQuery.getTotal(),list);
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        Map map = new HashMap();
        map.put("type",1);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号: " + outTradeNo);

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }
}