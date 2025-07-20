package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.models.auth.In;

public interface OrderService {
    Result<OrderSubmitVO> submit(OrdersSubmitDTO ordersSubmitDTO);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO);

    Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    Result<OrderVO> details(Long id);

    Result<OrderStatisticsVO> statistics();

    Result confirm(OrdersConfirmDTO ordersConfirmDTO );

    Result delivery(Long id);

    Result complete(Long id);

    Result cancel(OrdersCancelDTO ordersCancelDTO);

    Result rejection(OrdersRejectionDTO rejectionDTO);

    Result<PageResult> historyOrdersSearch(Integer page, Integer pageSize, Integer status);

    Result userCancel(Long id);

    Result repetition(Long id);
}
