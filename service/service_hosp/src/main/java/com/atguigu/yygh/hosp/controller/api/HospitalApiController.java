package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api(tags = "医院显示接口")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private ScheduleService scheduleService;

    

    //显示科室可以预约日期数据
    //医院编号+科室编号+分页参数
    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R getBookingSchedule(
            @PathVariable Integer page,
            @PathVariable Integer limit,
            @PathVariable String hoscode,
            @PathVariable String depcode) {
        Map<String, Object> map = scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode);
        return R.ok().data(map);
    }


    //1、条件查询医院列表
    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public R index(
            @PathVariable Integer page,
            @PathVariable Integer limit,
            HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> hospModel = hospitalService.selectPageHosp(page, limit, hospitalQueryVo);
        return R.ok().data("pages",hospModel);
    }

    //3、获取科室列表
    @Autowired
    private DepartmentService departmentService;
    @ApiOperation(value = "获取科室列表")
    @GetMapping("department/{hoscode}")
    public R index(@PathVariable String hoscode) {
        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
        return R.ok().data("list",list);
    }

    //2、医院名称模糊查询
    @ApiOperation(value = "根据医院名称获取医院列表")
    @GetMapping("findByHosname/{hosname}")
    public R findByHosname(@PathVariable String hosname) {
        List<Hospital> list = hospitalService.getHospLike(hosname);
        return R.ok().data("list",list);
    }

    //4、根据医院编号获取医院详情
    @ApiOperation(value = "医院预约挂号详情")
    @GetMapping("{hoscode}")
    public R item(
            @PathVariable String hoscode) {
        Map<String, Object> map = hospitalService.selctHospByHoscode(hoscode);
        return R.ok().data(map);
    }

    //5. 获取排班数据
    @ApiOperation("获取排版数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public R findScheduleList(@PathVariable String hoscode,
                              @PathVariable String depcode,
                              @PathVariable String workDate){
        List<Schedule> scheduleList = scheduleService.getScheduleDataDetail(hoscode, depcode, workDate);
        return R.ok().data("scheduleList",scheduleList);
    }

    //6、根据排班id获取排班的详情数据
    @ApiOperation(value = "获取排班详情")
    @GetMapping("getSchedule/{id}")
    public R getScheduleList(
            @PathVariable String id){
        Schedule schedule = scheduleService.getScheduleId(id);
        return R.ok().data("schedule",schedule);

    }

    //7、order远程调用使用--生成订单使用，根据排班id获取预约下单数据
    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }
}
