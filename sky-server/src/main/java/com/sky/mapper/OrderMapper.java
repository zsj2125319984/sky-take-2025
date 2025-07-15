package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     * @param orders
     */
    void save(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 分页查询
     * @param ordersPageQueryDTO
     * @return {@link Page }<{@link Orders }>
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询
     * @param id
     * @return {@link Orders }
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 根据状态查询订单数量
     * @param status
     * @return {@link Integer }
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer getCountByStatus(Integer status);

    /**
     * 根据状态和时间(条件为之前)查询
     * @param status
     * @param time
     * @return {@link List }<{@link Orders }>
     */
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeBefore(Integer status, LocalDateTime time);

    /**
     * 根据map查询营业额
     * @param map
     * @return {@link Double }
     */
    Double sumByMap(Map map);

    /**
     * 根据map查询订单数
     * @param map
     * @return {@link Integer }
     */
    Integer getCountByMap(Map map);

    /**
     * 根据时间查询销量top10
     * @param end
     * @param begin
     * @return {@link List }<{@link GoodsSalesDTO }>
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
