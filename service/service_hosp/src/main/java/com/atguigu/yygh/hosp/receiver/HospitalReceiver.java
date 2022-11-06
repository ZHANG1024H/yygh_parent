package com.atguigu.yygh.hosp.receiver;


import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.rabbit.RabbitService;
import com.atguigu.yygh.rabbit.constant.MqConst;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitService rabbitService;

    //接收订单模块发送过来的mq消息
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public void updateSchedule(OrderMqVo orderMqVo, Message message, Channel channel){
        //判断取消预约更新还是生成订单更新
        //判断orderMqVo里面是否有号数量，如果有就是生成订单更新，如果没有取消预约+1
        if (orderMqVo.getAvailableNumber()!=null){//生成订单更新
            //1、调用service方法更新排班号数量
            //根据排班id得到排班对象
            Schedule schedule = scheduleService.getScheduleId(orderMqVo.getScheduleId());
            //设置更新值
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
            //调用方法更新
            scheduleService.updateSchedule(schedule);
        }else {//取消预约+1
            Schedule schedule = scheduleService.getScheduleId(orderMqVo.getScheduleId());
            int availableNumber = schedule.getAvailableNumber().intValue() + 1;
            schedule.setAvailableNumber(availableNumber);
            scheduleService.updateSchedule(schedule);
        }
        //2、发送mq消息，短信发送
        MsmVo msmVo = orderMqVo.getMsmVo();
        if (msmVo != null){
            //发送mq消息
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }

    }

}
