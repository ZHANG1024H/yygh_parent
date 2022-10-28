package com.atguigu.yygh.common.result;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class R {

    private boolean success; //响应是否成功

    private Integer code; //响应码

    private String message; //返回消息

    private Map<String,Object> data = new HashMap<>();

    //构造私有化,防止别人乱调用乱修改。
    private R() {}

    //成功
    public static R ok() {
        R r = new R();
        r.setCode(ResultCode.SUCCESS);
        r.setMessage("成功");
        r.setSuccess(true);
        return r;
    }

    //失败
    public static R error() {
        R r = new R();
        r.setCode(ResultCode.ERROR);
        r.setMessage("失败");
        r.setSuccess(false);
        return r;
    }

    public R success(Boolean success){
        this.setSuccess(success);
        return this;
    }
    public R message(String message){
        this.setMessage(message);
        return this;
    }
    public R code(Integer code){
        this.setCode(code);
        return this;
    }

    //传key，value
    public R data(String key, Object value){
        this.data.put(key, value);
        return this;
    }
    //传map
    public R data(Map<String, Object> map){
        this.setData(map);
        return this;
    }

}
