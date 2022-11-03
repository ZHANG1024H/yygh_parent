package com.atguigu.yygh.user.service;


import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    // 根据微信的openid查询
    UserInfo getWxInfoByOpenid(String openid);

    //用户认证
    void userAuth(Long userId, UserAuthVo userAuthVo);

    //用户列表（条件分页查新）
    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    //2、用户详情
    Map<String, Object> show(Long userId);

    //3、认证审批
    void approval(Long userId, Integer authStatus);
}
