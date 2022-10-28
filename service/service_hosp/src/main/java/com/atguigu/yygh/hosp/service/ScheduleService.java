package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    //上传排班
    void saveSchedule(Map<String, Object> newObjectMap);

    //查询排班
    Page<Schedule> selectPageSchedule(int page, int limit, String hoscode, String depcode);

    //删除排班
    void remove(String hoscode, String hosScheduleId);

  //根据医院编号和科室编号查询可以预约日期数据
    Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode);

    //根据医院编号+科室编号+工作日期，查询科室中医生排班详细信息
    List<Schedule> getScheduleDataDetail(String hoscode, String depcode, String workDate);
}
