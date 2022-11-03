package com.atguigu.yygh.user.service;



import com.atguigu.yygh.model.user.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2022-11-01
 */
public interface PatientService extends IService<Patient> {

    //4.获取就诊人列表
    List<Patient> findAllUserId(Long userId);

    //5.根据id获取就诊人信息
    Patient getPatientId(Long id);
}
