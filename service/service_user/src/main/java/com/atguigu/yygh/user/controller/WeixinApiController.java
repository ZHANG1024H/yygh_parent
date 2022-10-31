package com.atguigu.yygh.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.ConstantPropertiesUtil;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

@Api(tags = "微信登陆二维码返回参数接口")
@Controller
@RequestMapping("/api/user/wx")
public class WeixinApiController {
    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 获取微信登录参数
     * 返回微信登录二维码相关参数
     */
    @GetMapping("getLoginParam")
    @ResponseBody //响应数据
    public R getLoginParam(){
        HashMap<String, Object> map = new HashMap<>();

        //appid
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);

        //scope
        map.put("scope","snsapi_login");

        // redirectUri 需要进行UrlEncode
        String wxOpenRedirectUrl = ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL;
        try {
            wxOpenRedirectUrl = URLEncoder.encode(wxOpenRedirectUrl,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        map.put("redirectUri",wxOpenRedirectUrl);

        return R.ok().data(map);
    }

    //得到扫码人信息
    @GetMapping("callback")
    public String callback(String code, String state, HttpSession session) {
        //1、得到授权临时票据code
        //2、拿着code + app_id + app_secret 请求微信固定地址，得到 openid 和 access_token
        try {
            StringBuffer stringBuffer = new StringBuffer()
                    .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                    .append("?appid=%s")
                    .append("&secret=%s")
                    .append("&code=%s")
                    .append("&grant_type=authorization_code");
            String url = String.format(stringBuffer.toString(),
                    ConstantPropertiesUtil.WX_OPEN_APP_ID,
                    ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                    code);
            String resultInfo = HttpClientUtils.get(url);
            System.out.println("resultInfo = " + resultInfo);

            //第一种，转map获取值
            /*HashMap map = JSONObject.parseObject(resultInfo, HashMap.class);
            String access_token = (String)map.get("access_token");
            String openid = (String)map.get("openid");*/

            //第二种转JSON对象获取值
            JSONObject jsonObject = JSONObject.parseObject(resultInfo);
            String access_token = jsonObject.getString("access_token");
            String openid = jsonObject.getString("openid");

            //3、拿着openid 和 access_token，再去请求微信另外一个固定地址，返回微信扫码人信息
            //第三步 拿着openid  和  access_token请求微信地址，得到扫描人信息
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openid=%s";

            String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);
            String userInfoResult = HttpClientUtils.get(userInfoUrl);

            //获取微信昵称和openid
            JSONObject userInfoObject = JSONObject.parseObject(userInfoResult);
            String nickname = userInfoObject.getString("nickname");

            //4、判断微信是否第一次扫码登录，如果是第一次添加（注册），
            // 根据微信的openid查询
            UserInfo userInfo = userInfoService.getWxInfoByOpenid(openid);
            if (userInfo == null){//第一次扫码登录
                userInfo = new UserInfo();
                userInfo.setNickName(nickname);
                userInfo.setOpenid(openid);
                userInfo.setStatus(1);
                userInfoService.save(userInfo);
            }
            //返回数据，放到map中
            HashMap<String, String> map = new HashMap<>();
            String name = userInfo.getName();
            if (StringUtils.isEmpty(name)){//第一次登录，添加
                name = userInfo.getNickName();
            }else {//不是第一次登录，
                name = userInfo.getPhone();
            }
            map.put("name",name);

            //5、第一次微信登录，绑定手机号,登录微信未绑定手机号也要注册
            //判断当前微信用户是否需要绑定手机号
            //获取手机号信息
            String phone = userInfo.getPhone();
            //判断逻辑：如果绑定，传递openid值，如果不绑定不传递
            //前端判断：有openid值绑定，没有不绑定
            if (StringUtils.isEmpty(phone)){
                map.put("openid",openid);
            }else {
                map.put("openid","");
            }

            //返回token
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("token",token);

            //跳转页面（如果绑定手机号跳转手机号页面，如果不需要绑定关闭弹框刷新页面
            return "redirect:http://localhost:3000/weixin/callback"+
                    "?token="+map.get("token")+
                    "&openid="+map.get("openid")+
                    "&name="+URLEncoder.encode(map.get("name"),"utf-8");

        } catch (Exception e) {
           e.printStackTrace();
        }
        return null;
    }
}
