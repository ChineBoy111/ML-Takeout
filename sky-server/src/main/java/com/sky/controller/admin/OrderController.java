package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "管理端订单相关接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索 返回分页结果
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索 获取分页结果")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索信息{}",ordersPageQueryDTO);
        return orderService.conditionSearch(ordersPageQueryDTO);
    }
    /**
     * 查询订单详情
     *   GET /admin/order/details/{id}
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable Long id){
        return orderService.details(id);
    }
    /**
     * 各个状态的订单数量统计
     *   GET /admin/order/statistics
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics(){
        return orderService.statistics();
    }
    /**
     * 接单
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("接单订单id：{}",ordersConfirmDTO);
        return orderService.confirm(ordersConfirmDTO);
    }
    /**
     * 拒单
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO rejectionDTO){
        log.info("拒单信息{}",rejectionDTO);
        return orderService.rejection(rejectionDTO);
    }
    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("取消订单{}",ordersCancelDTO);
        return orderService.cancel(ordersCancelDTO);
    }
    /**
     * 完成订单
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id){
        log.info("完成订单{}",id);
        return orderService.complete(id);
    }
    /**
     * 派送订单
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id){
        log.info("派送订单{}",id);
        return orderService.delivery(id);
    }
}
