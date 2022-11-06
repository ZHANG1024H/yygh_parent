package com.atguigu.yygh.msm.receiver;

import com.atguigu.yygh.rabbit.constant.MqConst;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MsmReceiver {

    //接收hosp模块发送过来的消息，进行消费
    @RabbitListener(bindings = @QueueBinding(
            //队列名称，+持久化
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),
            //
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            //路由key
            key = {MqConst.ROUTING_MSM_ITEM}
    ))
    //有消息过来就消费，无消息就监听
    public void send(MsmVo msmVo, Message message, Channel channel){
        //TODO 模拟流程，把消息获取到·输出
        System.out.println("短信发送了："+msmVo.getPhone());
    }
}
