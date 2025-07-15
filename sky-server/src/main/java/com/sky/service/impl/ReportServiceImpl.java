package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    UserMapper userMapper;

    /**
     * 销量排名统计
     * @param begin
     * @param end
     * @return {@link SalesTop10ReportVO }
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        //将日期转为日期和时间
        LocalDateTime beginDateTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(end, LocalTime.MAX);

        //查询top10列表
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper
                .getSalesTop10(beginDateTime,endDateTime);
        //将列表转化为SalesTop10ReportVO
        List<String> nameList = goodsSalesDTOList.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.toList());

        List<Integer> numberList = goodsSalesDTOList.stream()
                .map(GoodsSalesDTO::getNumber)
                .collect(Collectors.toList());
        //返回结果

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return {@link OrderReportVO }
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        //获得日期列表
        List<LocalDate> dateList = getDateList(begin,end);
        //获得有效订单数和订单数列表
        List<Integer> validOrderList = new ArrayList<>();
        List<Integer> orderList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //将日期转为日期和时间
            LocalDateTime beginDateTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endDateTime = LocalDateTime.of(date, LocalTime.MAX);

            Integer validOrderCount = getOrderCount(beginDateTime,endDateTime,Orders.COMPLETED);
            Integer orderCount = getOrderCount(beginDateTime,endDateTime,null);

            validOrderCount = (validOrderCount == null ? 0 : validOrderCount);
            orderCount = (orderCount == null ? 0 : orderCount);

            validOrderList.add(validOrderCount);
            orderList.add(orderCount);
        }
        //获得有效订单总数和订单总数
        Integer totalValidOrderCount = validOrderList.stream().reduce(Integer::sum).get();
        Integer totalOrderCount = orderList.stream().reduce(Integer::sum).get();

        //计算订单完成率
        Double orderCompleteRate = 0.0;
        if(!totalOrderCount.equals(0)){
            orderCompleteRate = totalValidOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .validOrderCountList(StringUtils.join(validOrderList,","))
                .orderCountList(StringUtils.join(orderList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompleteRate)
                .build();
    }

    /**
     * 查询订单数
     * @param beginDateTime
     * @param endDateTime
     * @param status
     * @return {@link Integer }
     */
    private Integer getOrderCount(LocalDateTime beginDateTime,
                                  LocalDateTime endDateTime, Integer status) {
        Map map = new HashMap();
        map.put("begin",beginDateTime);
        map.put("end",endDateTime);
        map.put("status",status);

        return orderMapper.getCountByMap(map);
    }

    /**
     * 获得日期列表
     * @param begin
     * @param end
     * @return {@link List }<{@link LocalDate }>
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        return dateList;
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return {@link UserReportVO }
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //获得日期列表
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //获得总用户数列表和新用户数列表
        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //将日期转为日期和时间
            LocalDateTime beginDateTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endDateTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("end",endDateTime);

            Integer totalUser = userMapper.countByMap(map);

            map.put("begin",beginDateTime);

            Integer newUser = userMapper.countByMap(map);

            totalUser = (totalUser == null ? 0 : totalUser);
            newUser = (newUser  == null ? 0 : newUser);
            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
        //返回结果
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return {@link TurnoverReportVO }
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //获得日期列表
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //获得对应营业额列表
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //将日期转为日期和时间
            LocalDateTime beginDateTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endDateTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询营业额
            Map map = new HashMap();
            map.put("begin",beginDateTime);
            map.put("end",endDateTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);
            turnover = (turnover == null ? 0.0 : turnover);
            turnoverList.add(turnover);
        }
        //返回结果

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }
}
