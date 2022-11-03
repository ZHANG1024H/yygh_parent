package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    //上传科室
    void saveDept(Map<String, Object> newObjectMap);

    //查询科室
    Page<Department> selectPageDept(int page, int limit, String hoscode, String depcode);

    //删除科室
    void remove(String hoscode, String depcode);

    //查询所有科室列表（大科室，小科室）
    List<DepartmentVo> findDeptTree(String hoscode);

    //科室
    Department getDepartment(String hoscode, String depcode);
}
