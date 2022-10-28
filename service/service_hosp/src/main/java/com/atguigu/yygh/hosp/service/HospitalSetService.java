package com.atguigu.yygh.hosp.service;


import com.atguigu.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 医院设置表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2022-10-17
 */
public interface HospitalSetService extends IService<HospitalSet> {

    //根据医院编号查询签名key
    String getHospSignKey(String hoscode);
}
