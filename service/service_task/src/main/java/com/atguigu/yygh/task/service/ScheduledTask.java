package com.atguigu.yygh.task.service;

import com.atguigu.yygh.rabbit.RabbitService;
import com.atguigu.yygh.rabbit.constant.MqConst;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@EnableScheduling //开启定时任务
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    // cron表达式设置执行规则
    //七域表达式
    //@Scheduled(cron = "0 0 20 * * ?")
    @Scheduled(cron = "0/5 * * * * ?")
    public void task1() {
        System.out.println(new Date().toLocaleString());
        //获取查询日期，获取第二天日期
        DateTime dateTime = new DateTime().plusDays(1);
        String dateString = dateTime.toString("yyyy-MM-dd");
        //发送mq消息
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,
                MqConst.ROUTING_TASK_8, dateString);
    }
}
