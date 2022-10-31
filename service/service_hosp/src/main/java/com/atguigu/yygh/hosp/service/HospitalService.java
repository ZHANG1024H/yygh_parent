package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    //添加医院数据
    void saveHosp(Map<String, Object> newObjectMap);

    //查询医院信息
    Hospital getHosp(String hoscode);

    //医院条件分页查询
    Page<Hospital> selectPageHosp(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    //更新医院上线状态
    void updateStatus(String id, Integer status);

    //查看医院详情
    Map<String, Object> showHosp(String id);

    //2、医院名称模糊查询
    List<Hospital> getHospLike(String hosname);

    //4、根据医院编号获取医院详情
    Map<String, Object> selctHospByHoscode(String hoscode);



}
