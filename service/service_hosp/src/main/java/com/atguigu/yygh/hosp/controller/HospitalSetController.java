package com.atguigu.yygh.hosp.controller;



import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.cj.log.Log;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-10-17
 */
@Api(tags =" 医院设置接口")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
/*@CrossOrigin //允许跨域*/
public class HospitalSetController {

    //注入Service
    @Autowired
    private HospitalSetService hospitalSetService;

    //8、医院设置锁定和解锁
    @ApiOperation("医院设置锁定和解锁")
    @PutMapping("lockHospitalSet/{id}/{status}")
    public R lockHospitalSet(@PathVariable Long id,
                             @PathVariable Integer status){
        //根据id查询医院设置信息
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        //设置状态
        hospitalSet.setStatus(status);
        //调用方法
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

    //7、批量删除
    /**
     * json对象那个格式：{...} -- java对象
     * json数组格式[1,2,3] -- java的list集合
     */
    @ApiOperation("批量删除")
    @PostMapping("deleteBatch")
    public R deleteBatch(@RequestBody List<Long> idList){
        //使用@RequestBody 获取json数组格式，数组有多个id值
        hospitalSetService.removeByIds(idList);
        return R.ok();
    }

    //5、添加接口
    @ApiOperation("添加")
    @PostMapping("saveHospSet")
    public R saveHospSet(@RequestBody HospitalSet hospitalSet){
        //为每个医院生成唯一字符串
        String signKey = System.currentTimeMillis() + "" + new Random().nextInt(1000);
        //设置到hospitalSet
        hospitalSet.setSignKey(signKey);
        //添加方法调用
        boolean is_success = hospitalSetService.save(hospitalSet);
        if (is_success){
            //把为这个医院生成唯一字符串保存到医院系统中
            //平台调用系统调用医院模拟系统同步接口实现
            //使用httpclient调用，封装了工具类
            HashMap<String, Object> map = new HashMap<>();
            map.put("sign",signKey);//签名密钥
            map.put("hoscode",hospitalSet.getHoscode());//医院编号
            //使用httpclient调用，封装了工具类
            JSONObject jsonObject = HttpRequestHelper.sendRequest(map, "http://localhost:9998/hospSet/updateSignKey");
            return R.ok();
        }else {
            return R.error();
        }
    }

    //6、修改接口 根据id查询
    @ApiOperation("根据id查询")
    @GetMapping("getHospSet/{id}")
    public R getHospSet(@PathVariable long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return R.ok().data("hospitalSet",hospitalSet);
    }

    //6.1、修改接口- 最终实现
    @ApiOperation("修改")
    @PutMapping("updateHospSet")
    public R updateHospSet(@RequestBody HospitalSet hospitalSet){
        boolean is_success = hospitalSetService.updateById(hospitalSet);
        if (is_success){
            return R.ok();
        }else {
            return R.error();
        }
    }

    /**
     * 请求体
     * @RequestBody(required = false)
     *  这个注解作用：
     *      传递数据时以json格式
     *      使用这个注解获取json格式数据，把json这个数据封装到对象里面
     *
     *      通俗来说：接受json格式数据进行封装
     *
     *      特点：这个注解使用时，请求方式不能get提交，因为get提交没有请求体
     */
    //4.1、条件分页查询
    @ApiOperation("条件分页查询方法二requestbody")
    @PostMapping("findPageQueryHospSet/{current}/{limit}")
    public R findPageQueryHospSet(@PathVariable long current,
                           @PathVariable long limit,
                           //required = false，表示参数值可以为空
                           @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo){
        //1、创建配置对象，传递current和limit两个参数
        Page<HospitalSet> pageParam = new Page<>(current,limit);
        //2、封装条件
        if (hospitalSetQueryVo == null){ //如果条件对象为空，查询全部
            //2.1、调用方法实现分页查询
            hospitalSetService.page(pageParam);
        }else { //如果对象条件不为空，进行条件查询
            //2.2、
            QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
            //判断条件值是否为空，不为空封装
            String hoscode = hospitalSetQueryVo.getHoscode();
            String hosname = hospitalSetQueryVo.getHosname();
            if (!StringUtils.isEmpty(hoscode)) {
                wrapper.eq("hoscode",hoscode);
            }
            if (!StringUtils.isEmpty(hosname)) {
                wrapper.like("hosname",hosname);
            }
            //获取更新时间
            String time = hospitalSetQueryVo.getUpdateTime();
            //降序排序
            wrapper.orderByDesc("update_time",time);
            //3、调用方法
            hospitalSetService.page(pageParam,wrapper);
        }
        //4、获取数据
        List<HospitalSet> list = pageParam.getRecords();
        long total = pageParam.getTotal();

        //5、返回数据方法一
        return R.ok().data("total",total).data("list",list);
    }

    //4、条件分页查询
    @ApiOperation("条件分页查询")
    @GetMapping("findPageQuery/{current}/{limit}")
    public R findPageQuery(@PathVariable long current,
                           @PathVariable long limit,
                           HospitalSetQueryVo hospitalSetQueryVo){
        //1、创建配置对象，传递current和limit两个参数
        Page<HospitalSet> pageParam = new Page<>(current,limit);
        //2、封装条件
        if (hospitalSetQueryVo == null){ //如果条件对象为空，查询全部
            //2.1、调用方法实现分页查询
            hospitalSetService.page(pageParam);
        }else { //如果对象条件不为空，进行条件查询
            //2.2、
            QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
            //判断条件值是否为空，不为空封装
            String hoscode = hospitalSetQueryVo.getHoscode();
            String hosname = hospitalSetQueryVo.getHosname();
            if (StringUtils.isEmpty(hoscode)) {
                wrapper.eq("hoscode",hoscode);
            }
            if (StringUtils.isEmpty(hosname)) {
                wrapper.like("hosname",hosname);
            }
            //3、调用方法
            hospitalSetService.page(pageParam,wrapper);
        }
        //4、获取数据
        List<HospitalSet> list = pageParam.getRecords();
        long total = pageParam.getTotal();

        //5、返回数据方法一
        return R.ok().data("total",total).data("list",list);
    }

    //3 分页查询（不带条件）
    //current当前页 limit每页显示记录数
    @ApiOperation("分页查询")
    @GetMapping("findPage/{current}/{limit}")
    public R findPage(@PathVariable long current,
                      @PathVariable long limit){
        //1、创建配置对象，传递current和limit两个参数
        Page<HospitalSet> pageParam = new Page<>(current,limit);
        //2、调用方法实现分页查询
        hospitalSetService.page(pageParam);
        //3、pageParam封装分页所有数据
        List<HospitalSet> list = pageParam.getRecords();
        long total = pageParam.getTotal();

        //4、返回数据方法一
        return R.ok().data("total",total).data("list",list);

        //4.1、返回数据方法二
        /*Map<String, Object> map = new HashMap<>();
        map.put("total",total);
        map.put("list",list);
        return R.ok().data(map);*/

    }

    //1、查询医院设置表中的所有数据
    //restful风格
    //查询所有医院设置
    @ApiOperation(value = "查询所有数据")
    @GetMapping("findAll")
    public R findAllHospSet(){
        //模拟异常
        try {
            int i = 9/0;
        }catch (Exception e){
            //手动抛异常
            throw new YyghException(20001,"执行自定义异常处理");
        }
        //调用service中的方法
        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("list",list);
    }

    //2、逻辑删除医院设置
    @ApiOperation(value = "逻辑删除")
    @DeleteMapping("remove/{id}")
    public R removeHospSet(@PathVariable Long id){
        boolean is_success = hospitalSetService.removeById(id);
        if (is_success){
            return R.ok();
        }else {
            return R.error();
        }
    }
}

