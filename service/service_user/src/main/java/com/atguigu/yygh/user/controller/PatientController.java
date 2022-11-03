package com.atguigu.yygh.user.controller;


import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 就诊人表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-11-01
 */
//就诊人管理接口
@Api(tags = "就诊人")
@RestController
@RequestMapping("/api/user/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    //1.添加就诊人
    @ApiOperation("添加就诊人")
    @PostMapping("auth/save")
    public R savePatient(@RequestBody Patient patient, HttpServletRequest request){
        //获取用户id
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return R.ok();
    }

    //2.修改就诊人
    @PostMapping("auth/update")
    public R updatePatient(@RequestBody Patient patient) {
        patientService.updateById(patient);
        return R.ok();
    }

    //3.删除就诊人
    @DeleteMapping("auth/remove/{id}")
    public R removePatient(@PathVariable Long id) {
        patientService.removeById(id);
        return R.ok();
    }

    //4.获取就诊人列表
    @GetMapping("auth/findAll")
    public R findAll(HttpServletRequest request) {
        //获取当前登录用户id
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> list = patientService.findAllUserId(userId);
        return R.ok().data("list",list);
    }

    //5.根据id获取就诊人信息
    @GetMapping("auth/get/{id}")
    public R getPatient(@PathVariable Long id) {
        Patient patient = patientService.getPatientId(id);
        return R.ok().data("patient",patient);
    }
}

