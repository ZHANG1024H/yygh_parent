package com.atguigu.yygh.msm.service;

public interface MsmService {

    //根据手机号发送手机短信
    boolean sendMsm(String phone, String code);
}
