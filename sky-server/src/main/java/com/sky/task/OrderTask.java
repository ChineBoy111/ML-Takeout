package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理未支付超时的订单
     */
    @Scheduled(cron = "0 0/1 * * * ? ") //每一分钟执行一次
    public void processTimeoutOrder() {
        log.info("处理超时订单:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15); //15分钟前的订单
        List<Orders> ordersList = orderMapper.processTimeoutOrder(Orders.PENDING_PAYMENT,time);
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("支付超时");
                orderMapper.update(orders);
            }
        }
    }
    /**
     * 处理一直处于派送中的状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ? ") //每天凌晨一点处理
    public void processDeliveryOrder() {
        log.info("处理派送中的订单:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        List<Orders> ordersList = orderMapper.processTimeoutOrder(Orders.DELIVERY_IN_PROGRESS,time);
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
