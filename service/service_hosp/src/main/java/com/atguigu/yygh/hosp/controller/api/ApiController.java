package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.hosp.utils.MD5;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.data.domain.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    //3.上传排班
    @ApiOperation(value = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        scheduleService.saveSchedule(newObjectMap);
        return Result.ok();
    }

    //3.1、查询排班
    @ApiOperation(value = "获取排班分页列表")
    @PostMapping("schedule/list")
    public Result schedule(HttpServletRequest request) {
        //获取医院模拟系统传递数据
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(parameterMap);

        //必须参数校验 略
        String hoscode = (String)newObjectMap.get("hoscode");
        //非必填
        String depcode = (String)newObjectMap.get("depcode");

        int page = StringUtils.isEmpty(newObjectMap.get("page")) ? 1 : Integer.parseInt((String)newObjectMap.get("page"));
        int limit = StringUtils.isEmpty(newObjectMap.get("limit")) ? 10 : Integer.parseInt((String)newObjectMap.get("limit"));

        //调用service方法查询排班
        Page<Schedule> pageScheduleModel = scheduleService.selectPageSchedule(page,limit,hoscode,depcode);

        return Result.ok(pageScheduleModel);
    }

    //3.2 删除排班
    @ApiOperation(value = "删除排班")
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        //获取医院模拟系统传递数据
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验 略
        String hoscode = (String) newObjectMap.get("hoscode");
        //非必填
        String hosScheduleId = (String) newObjectMap.get("hosScheduleId");
        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }


    //2.上传科室
    @ApiOperation("上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        //获取医院模拟系统传递数据
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(parameterMap);

        departmentService.saveDept(newObjectMap);

        return Result.ok();
    }

    //2.1、科室获取分页列表
    @ApiOperation("科室获取分页列表")
    @PostMapping("department/list")
    public Result department(HttpServletRequest request){
        //获取医院模拟系统传递数据
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(parameterMap);

        //必须参数校验
        String hoscode = (String) newObjectMap.get("hoscode");
        //非必填
        String depcode = (String) newObjectMap.get("depcode");
        int page = StringUtils.isEmpty(newObjectMap.get("page")) ? 1 : Integer.parseInt((String)newObjectMap.get("page"));
        int limit = StringUtils.isEmpty(newObjectMap.get("limit")) ? 10 : Integer.parseInt((String)newObjectMap.get("limit"));

        //调用service方法
        Page<Department> pageModel = departmentService.selectPageDept(page, limit, hoscode, depcode);
        return Result.ok(pageModel);
    }

    //2.2 删除科室
    @ApiOperation(value = "删除科室")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        //获取医院模拟系统传递数据
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(parameterMap);
        //必须参数校验
        String hoscode = (String) newObjectMap.get("hoscode");
        //非必填
        String depcode = (String) newObjectMap.get("depcode");
        departmentService.remove(hoscode,depcode);
        return Result.ok();
    }

    //1.1、获取医院信息
    @ApiOperation("获取医院信息")
    @PostMapping("hospital/show")
    public Result hospital(HttpServletRequest request){
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //获取医院编号
        String hoscode = (String) newObjectMap.get("hoscode");
        Hospital hospital = hospitalService.getHosp(hoscode);
        return Result.ok(hospital);
    }

    //1、上传医院信息（添加医院）
    @ApiOperation(value = "上传医院")
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request){
        //1.获取提交参数，封装map集合里面
        Map<String, String[]> parameterMap = request.getParameterMap();

        //2.为了后面的操作方便，进行转换：Map<String, String[]> --> Map<String, String>
        Map<String, Object> newObjectMap = HttpRequestHelper.switchMap(parameterMap);

        //4、添加签名校验
        //获取医院模拟系统传递sign
        String signHospital = (String) newObjectMap.get("sign");
        //查询当前医院在平台保存sign
        String hoscode = (String) newObjectMap.get("hoscode");
        String signYygh = hospitalSetService.getHospSignKey(hoscode);

        //对比两个sign是否相同
        //signHospital和signYygh比较
        String MD5SignYygh = MD5.encrypt(signYygh);
        if (!signHospital.equals(MD5SignYygh)){
            throw new YyghException(20001,"签名校验失败");
        }

        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData = (String)newObjectMap.get("logoData");
        logoData = logoData.replaceAll(" ","+");
        newObjectMap.put("logoData",logoData);


        //3、调用service方法添加
        hospitalService.saveHosp(newObjectMap);
        return Result.ok();
    }
}

















