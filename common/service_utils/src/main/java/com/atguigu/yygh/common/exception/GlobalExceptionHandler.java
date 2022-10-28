package com.atguigu.yygh.common.exception;

import com.atguigu.yygh.common.result.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    //1、全局异常
    @ExceptionHandler(Exception.class)
    @ResponseBody//返回json数据
    public R error(Exception e){
        System.out.println("全局异常处理器处理");
        e.printStackTrace();
        return R.error().message("执行全局异常处理");
    }

    //2、特定异常 ArithmeticException
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody//返回json数据
    public R error(ArithmeticException e){
        System.out.println("特定异常处理器处理");
        e.printStackTrace();
        return R.error().message("执行特定异常处理");
    }

    //3、自定义异常
    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public R error(YyghException e){
        e.printStackTrace();
        return R.error().message("执行自定义异常处理");
    }
}
