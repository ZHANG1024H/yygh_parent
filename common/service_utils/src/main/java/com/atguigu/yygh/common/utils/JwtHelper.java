package com.atguigu.yygh.common.utils;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

public class JwtHelper {
    //token字符串过期时间，为了测试设置的时间长一些，实际根据需要设置
    private static long tokenExpiration = 24*60*60*1000;

    //签名密钥。用于加密编码使用
    private static String tokenSignKey = "123456";

    //根据传递的参数，生成token字符串
    public static String createToken(Long userId, String userName) {
        String token = Jwts.builder()
                //分类
                .setSubject("YYGH-USER")

                //设置字符串的过期时间
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))

                //私有部分，设置用户信息
                .claim("userId", userId)
                .claim("userName", userName)

                //根据密钥使用HS512进行编码加密
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)

                //把所有信息压缩在一行显示
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    //根据token字符串，从token获取userid
    public static Long getUserId(String token) {
        if(StringUtils.isEmpty(token)) return null;
        //根据密钥解码得到私有部分
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();

        Integer userId = (Integer)claims.get("userId");
        return userId.longValue();
    }

    //根据token字符串，从token获取username
    public static String getUserName(String token) {
        if(StringUtils.isEmpty(token)) return "";
        Jws<Claims> claimsJws
                = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();

        return (String)claims.get("userName");
    }

    public static void main(String[] args) {
        String token = JwtHelper.createToken(1L, "55");
        System.out.println(token);
        System.out.println(JwtHelper.getUserId(token));
        System.out.println(JwtHelper.getUserName(token));
    }
}
