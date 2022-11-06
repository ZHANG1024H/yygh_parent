package com.atguigu.yygh.orders.service;


import com.atguigu.yygh.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2022-11-04
 */
public interface OrderInfoService extends IService<OrderInfo> {

    //1、根据排班id和就诊人id生成挂号订单
    Long createOrder(String scheduleId, Long patientId);

    //2、根据订单id查询订单详情
    OrderInfo getOrderInfo(Long orderId);

    //3、取消预约
    boolean cancelOrder(Long orderId);

    //4、就医提醒
    void tips(String dateString);
}
