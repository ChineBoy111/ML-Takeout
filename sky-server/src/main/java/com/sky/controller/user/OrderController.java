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

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户端订单相关接口")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    /**
     * 新增订单
     */
    @PostMapping("/submit")
    @ApiOperation("新增订单接口")
    public Result<OrderSubmitVO> addOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单信息{}",ordersSubmitDTO);
        return orderService.submit(ordersSubmitDTO);
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
     * 历史订单查询
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询接口")
    public Result<PageResult> historyOrders(Integer page, Integer pageSize, Integer status){
        log.info("历史订单查询，页码{}，页大小{},订单状态",page,pageSize, status);
        return orderService.historyOrdersSearch(page,pageSize,status);
    }
    /**
     * 查询订单详情
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情接口")
    public Result<OrderVO> orderDetail(@PathVariable Long id){
        log.info("查询订单详情，订单id{}",id);
        return orderService.details(id);
    }
    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单接口")
    public Result cancel(@PathVariable Long id){
        log.info("取消订单，订单id{}",id);
        return orderService.userCancel(id);
    }
    /**
     * 用户再来一单
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("用户再来一单接口")
    public Result repetition(@PathVariable Long id){
        log.info("再来一单，订单id{}",id);
        return orderService.repetition(id);
    }
}
