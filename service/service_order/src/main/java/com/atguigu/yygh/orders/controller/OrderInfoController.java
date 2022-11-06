package com.atguigu.yygh.orders.controller;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.orders.service.OrderInfoService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-11-04
 */
@RestController
//Path=/*/order/**
@RequestMapping("/api/order/orderInfo")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    //1、根据排班id和就诊人id生成挂号订单
    @ApiOperation("创建订单")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public R submitOrder(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable String scheduleId,
            @ApiParam(name = "patientId", value = "就诊人id", required = true)
            @PathVariable Long patientId) {
        //返回订单id
        Long orderId = orderInfoService.createOrder(scheduleId, patientId);
        return R.ok().data("orderId", orderId);
    }

    //2、根据订单id查询订单详情
    @GetMapping("auth/getOrders/{orderId}")
    public R getOrders(@PathVariable Long orderId) {
        OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
        return R.ok().data("orderInfo",orderInfo);
    }

    //3、取消预约
    @ApiOperation(value = "取消预约")
    @GetMapping("auth/cancelOrder/{orderId}")
    public R cancelOrder(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @PathVariable("orderId") Long orderId) {
        boolean flag = orderInfoService.cancelOrder(orderId);
        return R.ok().data("flag",flag);
    }
}

