package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
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
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private ShoppingCartServiceImpl shoppingCartService;
    @Autowired
    private WebSocketServer webSocketServer;

    @Transactional
    public Result<OrderSubmitVO> submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理各种业务异常(地址簿为空、购物车数据为空)
        AddressBook addressbook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressbook == null) {
            //地址簿为空
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            //购物车数据为空
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表中插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setAddress(addressbook.getDetail());
        orders.setConsignee(addressbook.getConsignee());
        orders.setPhone(addressbook.getPhone());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);

        orderMapper.insert(orders);

        //向订单详情里面插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart cart : shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        //清空当前用户的购物车数据
        shoppingCartMapper.cleanCartShopping(userId);
        //封装VO返回结果
        OrderSubmitVO orderSubmitvo = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .build();


        return Result.success(orderSubmitvo);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO)  {
        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));

        //跳过微信支付
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return new OrderPaymentVO();
    }

    @Override
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //开始分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.conditionSearch(ordersPageQueryDTO);

        List<OrderVO> orderVOList = getOrderVOList(page);
        return Result.success(new PageResult(page.getTotal(),orderVOList));
    }
    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);
                //封装订单地址
                orderVO.setAddress(getAddrass(orderVO.getAddressBookId()));
                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailListByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    //获取订单配送地址
    private String getAddrass(Long addressBookId){
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        String address = addressBook.getProvinceName() + addressBook.getCityName() +
                addressBook.getDistrictName() + addressBook.getDetail();
        return address;
    }
    public Result<OrderVO> details(Long id) {
        OrderVO orderVO = new OrderVO();
        //根据订单ID查询订单数据
        Orders orders = orderMapper.getOrderById(id);
        orders.setAddress(getAddrass(orders.getAddressBookId()));
        BeanUtils.copyProperties(orders,orderVO);
        //根据订单ID查询订单详情 查询N条数据
        List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailListByOrderId(id);
        orderVO.setOrderDetailList(orderDetailList);
        //将订单name拼接成字符串
        String orderDishes = orderDetailList.stream().
                map(OrderDetail::getName)
                .collect(Collectors.joining(","));
        orderVO.setOrderDishes(orderDishes);
        return Result.success(orderVO);
    }

    @Override
    public Result<OrderStatisticsVO> statistics() {
        //查询待接单数量
        int toBeConfirmed = orderMapper.getStatusCount(Orders.TO_BE_CONFIRMED);
        //查询待派送数量
        int toBeSend  = orderMapper.getStatusCount(Orders.CONFIRMED);
        //查询派送中数量
        int deliveryInProgress  = orderMapper.getStatusCount(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(toBeSend);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return Result.success(orderStatisticsVO);
    }

    @Override
    public Result confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = new Orders();
        orders.setId(ordersConfirmDTO.getId());
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
        return Result.success();
    }

    @Override
    public Result delivery(Long id) {
        //派送订单 将带派送订单 状态 改为 派送中
        Orders orderById = orderMapper.getOrderById(id);
        if(Objects.equals(orderById.getStatus(), Orders.CONFIRMED)){
            orderById.setStatus(Orders.DELIVERY_IN_PROGRESS);
            orderMapper.update(orderById);
        }else{
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        return Result.success();
    }

    @Override
    public Result complete(Long id) {
        //将派送中的订单 状态 更改为完成
        Orders orderById = orderMapper.getOrderById(id);
        if(Objects.equals(orderById.getStatus(), Orders.DELIVERY_IN_PROGRESS)){
            orderById.setStatus(Orders.COMPLETED);
            //完成时间
            orderById.setDeliveryTime(LocalDateTime.now());
            orderMapper.update(orderById);

        }else{
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        return Result.success();
    }

    @Override
    public Result cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orderById = orderMapper.getOrderById(ordersCancelDTO.getId());
        //在已接单的情况下 可以取消订单

        //支付状态
        Integer payStatus = orderById.getPayStatus();
        if(payStatus == Orders.PAID){
            //需要退款
            orderById.setPayStatus(Orders.REFUND);
        }
        orderById.setStatus(Orders.CANCELLED);
        //添加取消原因
        orderById.setCancelReason(ordersCancelDTO.getCancelReason());
        //添加取消时间
        orderById.setCancelTime(LocalDateTime.now());
        orderMapper.update(orderById);

        return Result.success();
    }

    @Override
    public Result rejection(OrdersRejectionDTO rejectionDTO) {
        Orders orderById = orderMapper.getOrderById(rejectionDTO.getId());

        //在待接单的情况下 可以拒单
        if(Objects.equals(orderById.getStatus(), Orders.TO_BE_CONFIRMED)){
            //支付状态
            Integer payStatus = orderById.getPayStatus();
            if(payStatus == Orders.PAID){
                //需要退款
                orderById.setPayStatus(Orders.REFUND);
            }
            orderById.setStatus(Orders.CANCELLED);
            //添加拒单原因
            orderById.setRejectionReason(rejectionDTO.getRejectionReason());
            orderById.setCancelTime(LocalDateTime.now());
            orderMapper.update(orderById);
        }else{
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        return Result.success();
    }

    //用户订单历史查询
    public Result<PageResult> historyOrdersSearch(Integer page, Integer pageSize, Integer status) {
        //开启分页
        PageHelper.startPage(page, pageSize);
        Long userId = BaseContext.getCurrentId();
        //查询订单表中关于用户Id的所有满足状态的订单，再通过订单Id去订单详情表中查询详细的订单数据
        Page<OrderVO> pageOrders = orderMapper.historyOrdersSearch(userId, status);
        pageOrders.getResult().forEach(order -> {
            List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailListByOrderId(order.getId());
            order.setOrderDetailList(orderDetailList);
        });


        return Result.success(new PageResult(pageOrders.getTotal(), pageOrders.getResult()));
    }

    /**
     * 用户取消订单
     * @param id
     * @return
     */
    public Result userCancel(Long id) {
        Orders orderById = orderMapper.getOrderById(id);
        //检验订单是否存在
        if(orderById == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if(orderById.getStatus() > 2 ) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(id);
        // 订单处于待接单状态下取消，需要进行退款
        if (orderById.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
        return Result.success();
    }

    /**
     * 用户再来一单
     * @param id
     * @return
     */
    public Result repetition(Long id) {
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getOrderDetailListByOrderId(id);

        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
        return Result.success();
    }

    @Override
    public void reminder(Long id) {
        Orders orderById = orderMapper.getOrderById(id);
        //检验订单是否存在
        if(orderById == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //通过websocket向客户端发送消息
        Map map = new HashMap();
        map.put("type", 2);//1来单提醒 2 催单
        map.put("orderId", orderById.getId());
        map.put("content", "订单号:"+ orderById.getNumber());
        String json = JSON.toJSONString(map);
        System.out.println( "向客户端发送消息:"+json);
        webSocketServer.sendToAllClient(json);
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
//        Orders orders = Orders.builder()
//                .id(ordersDB.getId())
//                .status(Orders.TO_BE_CONFIRMED)
//                .payStatus(Orders.PAID)
//                .checkoutTime(LocalDateTime.now())
//                .build();
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .tablewareNumber(ordersDB.getTablewareNumber()).packAmount(ordersDB.getPackAmount()).build();
        orderMapper.update(orders);

        //通过websocket向客户端发送消息
        Map map = new HashMap();
        map.put("type", 1);//1来单提醒 2 催单
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号:"+ outTradeNo);
        String json = JSON.toJSONString(map);
        System.out.println( "向客户端发送消息:"+json);
        webSocketServer.sendToAllClient(json);

    }
}
