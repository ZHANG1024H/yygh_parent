package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository  extends MongoRepository<Department,String> {


    //上传科室
    Department getDeptByHoscodeAndDepcode(String hoscode, String depcode);
}
