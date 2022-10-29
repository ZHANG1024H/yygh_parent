package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "医院显示接口")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {

    @Autowired
    private HospitalService hospitalService;

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

    //获取科室列表
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
}
