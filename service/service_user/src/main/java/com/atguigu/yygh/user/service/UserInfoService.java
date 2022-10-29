package com.atguigu.yygh.user.service;


import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2022-10-29
 */
public interface UserInfoService extends IService<UserInfo> {

    //登录接口+手机验证码
    Map<String, Object> loginUser(LoginVo loginVo);
}
