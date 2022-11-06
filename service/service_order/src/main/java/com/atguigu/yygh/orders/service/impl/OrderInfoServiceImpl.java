package com.atguigu.yygh.orders.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.hosp.client.HospitalFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.orders.mapper.OrderInfoMapper;
import com.atguigu.yygh.orders.service.OrderInfoService;
import com.atguigu.yygh.orders.service.WeixinService;
import com.atguigu.yygh.orders.utils.HttpRequestHelper;
import com.atguigu.yygh.rabbit.RabbitService;
import com.atguigu.yygh.rabbit.constant.MqConst;
import com.atguigu.yygh.user.client.PatientFeignClient;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-11-04
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private WeixinService weixinService;

    //根据排班id和就诊人id生成挂号订单
    @Override
    public Long createOrder(String scheduleId, Long patientId) {
        //1.根据就诊人id获取就诊人数据-远程调用
        Patient patient = patientFeignClient.getPatient(patientId);
        //2、根据排班id 获取排班数据-远程调用
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);

        //3、时候httpClient调用医院系统下单接口
        //如果医院系统接口返回失败，不能下单

        //4、如果医院系统接口返回成功，得到医院接口返回数据
        //封装医院下单接口参数
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",scheduleOrderVo.getHoscode());
        paramMap.put("depcode",scheduleOrderVo.getDepcode());
        paramMap.put("hosScheduleId",scheduleOrderVo.getHosScheduleId());
        paramMap.put("reserveDate",new DateTime(scheduleOrderVo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", scheduleOrderVo.getReserveTime());
        paramMap.put("amount",scheduleOrderVo.getAmount()); //挂号费用
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        //String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", "");
        //使用httpClient工具类中的方法进行调用
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/submitOrder");

        //判断医院系统下单是否成功 判断code是否是200
        if (result.getInteger("code")==200){//成功

            //获取医院系统接口返回数据
            JSONObject jsonObject = result.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            //预约序号
            Integer number = jsonObject.getInteger("number");
            //取号时间
            String fetchTime = jsonObject.getString("fetchTime");
            //取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");

            //把上面三部分数据添加订单表，生成订单
            OrderInfo orderInfo = new OrderInfo();
            //设置排版数据
            //代码解释：把一个对象中的值复制到另一个对象中去，左边的值复制到右边
            //使用此方法的前提条件一个对象是有值的，一个对象是空的
            BeanUtils.copyProperties(scheduleOrderVo,orderInfo);

            //设置其他的数据，set进orderInfo
            //设置添加数据--就诊人数据
            //订单号
            String outTradeNo = System.currentTimeMillis() + ""+ new Random().nextInt(100);
            orderInfo.setOutTradeNo(outTradeNo);//订单交易号
            orderInfo.setScheduleId(scheduleId);//排班id
            orderInfo.setUserId(patient.getUserId());//用户id
            //就诊人信息
            orderInfo.setPatientId(patientId);
            orderInfo.setPatientName(patient.getName());
            orderInfo.setPatientPhone(patient.getPhone());
            //订单状态
            orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
            //设置添加数据--医院接口返回数据
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            //调用方法添加到订单表中
            baseMapper.insert(orderInfo);

            //5、整合RabbitMQ,发送消息
            //（1）根据医院系统接口返回号数量，更新mongodb
            //（2）给挂号成功就诊人发送短信
            //排班可预约数
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = jsonObject.getInteger("availableNumber");
            //封装mq消息对象
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);//排班id
            orderMqVo.setReservedNumber(reservedNumber);//可预约的数量
            orderMqVo.setAvailableNumber(availableNumber);//剩余数量
            //MsmVo封装
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            orderMqVo.setMsmVo(msmVo);
            //发送mq消息
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);


            //6、返回订单id
            return orderInfo.getId();
        }else {//失败
            throw new YyghException(20001,"挂号失败");
        }
    }

    //2、根据订单id查询订单详情
    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        OrderInfo orderInfo = this.packOrderInfo(baseMapper.selectById(orderId));
        return orderInfo;
    }
    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }


    //3、取消预约
    @Override
    public boolean cancelOrder(Long orderId) {
        //1、根据orderId查询订单信息
        OrderInfo orderInfo = this.getOrderInfo(orderId);
        //2、当前日期时间是否过了规定退号时间
        //获取退号时间
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
/*        if (quitTime.isBeforeNow()){
            throw new YyghException(20001,"不能退号");
        }*/

        //3、调用医院系统退号接口进行操作
        //封装医院接口需要的参数
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());//医院编号
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());//排班id
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        reqMap.put("sign", "");
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, "http://localhost:9998/order/updateCancelStatus");
        //3.1、根据医院系统退号接口返回值，决定后续流程
        if (result.getInteger("code")!=200){//不能退号
            throw new YyghException(20001,"不能退号");
        }else {//返回成功
            //4、医院系统退号接口返回成功
            Integer orderStatus = orderInfo.getOrderStatus();
            if (orderStatus.intValue() == OrderStatusEnum.PAID.getStatus().intValue()){//已经支付
                //4.2、如果订单已经支付，进行微信退款，退款成功之后，修改状态（订单状态已经取消）
                //退款时候，向退款记录表添加退款记录
                boolean isRefund = weixinService.refund(orderId);
                if (!isRefund){ //false
                    throw new YyghException(20001,"退款失败");
                }
            }
            //4.1、判断当前号是否已经支付，如果没有支付，直接修改状态（订单状态已经取消）
            //订单状态已经取消
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            baseMapper.updateById(orderInfo);

            //5、取消预约成功，整合RabbitMQ
            //更新号数量+1
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(orderInfo.getScheduleId());
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            orderMqVo.setMsmVo(msmVo);
            //发送mq消息
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        }
        return true;
    }

    //4、就医提醒
    @Override
    public void tips(String dateString) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getReserveDate,dateString);
        //订单状态不是-1  ne 不等于
        wrapper.ne(OrderInfo::getOrderStatus,OrderStatusEnum.CANCLE.getStatus());
        List<OrderInfo> orderInfoList = baseMapper.selectList(wrapper);

        //遍历查询集合
        for (OrderInfo orderInfo:orderInfoList) {
            //得到每个订单手机号
            String phone = orderInfo.getPatientPhone();

            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(phone);
            //发送mq
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }
}
