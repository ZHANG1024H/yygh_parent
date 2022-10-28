package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/hosp")
/*@CrossOrigin //允许跨域*/
public class LoginController {
    //login
    @PostMapping("login")
    public R login(){
        return R.ok().data("token","admin-token");
    }

    //info
    @GetMapping("info")
    public R info(){
        return R.ok().data("roles","admin")
                .data("introduction","I am a super administrator")
                .data("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
                .data("name","Super Admin");
    }
}





























