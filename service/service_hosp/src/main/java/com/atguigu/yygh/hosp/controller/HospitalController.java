package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "医院接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
/*@CrossOrigin*/
public class HospitalController {
    @Autowired
    private HospitalService hospitalService;

    //1、医院条件分页查询
    @ApiOperation("获取分页列表")
    @GetMapping("{page}/{limit}")
    public R index(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo) {
        //调用service方法
        Page<Hospital> pageModel = hospitalService.selectPageHosp(page,limit,hospitalQueryVo);
        return R.ok().data("pages",pageModel);
    }

    //2、更新医院状态
    @ApiOperation("更新上线状态")
    @GetMapping("updateStatus/{id}/{status}")
    public R lock(@ApiParam(name = "id",value = "医院id",required = true)
                  @PathVariable("id") String id,
                  @ApiParam(name = "status",value = "状态（0：未上线 1：已上线）",required = true)
                  @PathVariable("status") Integer status){
        hospitalService.updateStatus(id,status);
        return R.ok();
    }

    //3、查看医院详情
    @ApiOperation("查看医院详情")
    @GetMapping("show/{id}")
    public R show(@ApiParam(name = "id",value = "医院id", required = true)
                  @PathVariable("id") String id){
        Map<String,Object> map = hospitalService.showHosp(id);
        return R.ok().data(map);
    }

}
