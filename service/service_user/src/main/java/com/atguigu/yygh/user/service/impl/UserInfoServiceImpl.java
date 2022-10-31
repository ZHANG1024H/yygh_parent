package com.atguigu.yygh.user.service.impl;


import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-10-29
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    //登录接口+手机验证码
    @Override
    public Map<String, Object> loginUser(LoginVo loginVo) {

        //1、获取手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //2、手机号和验证码做非空判断
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)){
            throw new YyghException(20001,"数据为空");
        }
        //3、验证码校验过程
        // 输入验证码和redis验证码比对
        String redisCode = redisTemplate.opsForValue().get(phone);
        if (!code.equals(redisCode)){
            throw new YyghException(20001,"验证码校验失败");
        }

        //4、判断手机号是否第一次登录，根据手机号查询数据库
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("phone",phone);
        UserInfo userInfo = baseMapper.selectOne(wrapper);
        //5、如果是第一次使用手机号，进行注册，添加数据到用户表
        if (userInfo == null){
            //添加数据到用户表
            userInfo = new UserInfo();
            userInfo.setName("");
            userInfo.setPhone(phone);
            userInfo.setStatus(1);
            baseMapper.insert(userInfo);
        }

        //6、校验用户是否被禁用
        if (userInfo.getStatus() == 0){
            throw new YyghException(20001,"用户已被禁用");
        }
        //7、返回登录相关信息
        Map<String,Object> map = new HashMap<>();
        //用户显示名称
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)){
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)){
            name = userInfo.getPhone();
        }
        map.put("name",name);
        //生成token字符串
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token",token);

        return map;
    }

    // 根据微信的openid查询数据
    @Override
    public UserInfo getWxInfoByOpenid(String openid) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getOpenid,openid);
        //调用方法查询
        UserInfo userInfo = baseMapper.selectOne(wrapper);
        return userInfo;
    }


}













