package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    Result<OrderSubmitVO> submit(OrdersSubmitDTO ordersSubmitDTO);
}
