package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import com.sky.vo.SalesTop10ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);

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

    Page<Orders> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);
    @Select("select * from orders where id = #{id}")
    Orders getOrderById(Long id);

    @Select("select count(id) from orders where status = #{status}")
    int getStatusCount(Integer status);

    Page<OrderVO> historyOrdersSearch(Long userId, Integer status);
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> processTimeoutOrder(Integer status, LocalDateTime time);

    Double sumByMap(Map map);

    Integer countByMap(Map map);

    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
