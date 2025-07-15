package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 统计分析相关接口
 */
@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "统计分析相关接口")
public class ReportController {

    @Autowired
    ReportService reportService;

    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverStatistics
            (@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
             @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("营业额统计: {} ~ {}",begin,end);
        TurnoverReportVO turnoverReportVO = reportService.turnoverStatistics(begin,end);

        return Result.success(turnoverReportVO);
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return {@link Result }<{@link UserReportVO }>
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计")
    public Result<UserReportVO> userStatistics
            (@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
             @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("用户统计: {} ~ {}",begin,end);
        UserReportVO userReportVO = reportService.userStatistics(begin,end);

        return Result.success(userReportVO);
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return {@link Result }<{@link OrderReportVO }>
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("订单统计: {} ~ {}",begin,end);
        OrderReportVO orderReportVO = reportService.ordersStatistics(begin,end);

        return Result.success(orderReportVO);
    }

    /**
     * 销量排名统计
     * @return {@link Result }<{@link SalesTop10ReportVO }>
     */
    @GetMapping("/top10")
    @ApiOperation("销量排名统计")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("销量排名统计: {} ~ {}",begin,end);
        SalesTop10ReportVO salesTop10ReportVO = reportService.top10(begin,end);

        return Result.success(salesTop10ReportVO);
    }
}
