package com.atguigu.yygh.orders.service.impl;


import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.orders.mapper.PaymentInfoMapper;
import com.atguigu.yygh.orders.service.OrderInfoService;
import com.atguigu.yygh.orders.service.PaymentInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * <p>
 * 支付信息表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-11-04
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    @Autowired
    private OrderInfoService orderInfoService;

    //2、向支付记录表添加支付记录（状态：正在支付）
    @Override
    public void savePaymentInfo(OrderInfo orderInfo) {
        //判断订单是否添加支付记录
        LambdaQueryWrapper<PaymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentInfo::getOrderId,orderInfo.getId());
        Integer count = baseMapper.selectCount(wrapper);
        if (count > 0){
            return;
        }
        //添加
        //保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(1);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        //支付状态：支付中
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(orderInfo.getAmount());
        baseMapper.insert(paymentInfo);

    }

    //TODO 根据订单交易号，更新订单状态和支付记录状态：已经支付
    @Override
    public void paySuccess(String out_trade_no, Map<String, String> resultMap) {
        //1、根据交易号更新订单表订单状态;yijingzhifu
        //根据交易号查询订单信息，设置修改数据，调用方法更新
        LambdaQueryWrapper<OrderInfo> wrapperOrderInfo = new LambdaQueryWrapper<>();
        wrapperOrderInfo.eq(OrderInfo::getOutTradeNo,out_trade_no);
        OrderInfo orderInfo = orderInfoService.getOne(wrapperOrderInfo);
        //设置修改数据，
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        //调用方法更新
        orderInfoService.updateById(orderInfo);

        //2、根据交易号，更新支付记录：已经支付
        LambdaQueryWrapper<PaymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentInfo::getOutTradeNo,out_trade_no);
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapper);

        //设置修改值
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());//支付状态:已经支付
        //TradeNo 交易编码，在微信退款时会使用
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(resultMap.toString());
        baseMapper.updateById(paymentInfo);
    }

    //1、根据orderId查询支付记录表，获取支付记录
    @Override
    public PaymentInfo getPaymentInfoByOrderId(Long orderId) {
        LambdaQueryWrapper<PaymentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentInfo::getOrderId,orderId);
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapper);
        return paymentInfo;
    }
}
