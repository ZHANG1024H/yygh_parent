package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.ClientSessionException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    //医院名称可以从总获取
    @Autowired
    private HospitalService hospitalService;

    //上传排班
    @Override
    public void saveSchedule(Map<String, Object> newObjectMap) {

        //newObjectMap --> Department
        String jsonString = JSONObject.toJSONString(newObjectMap);
        Schedule schedule = JSONObject.parseObject(jsonString, Schedule.class);

        //根据医院编号 和 排班编号查询
       Schedule existSchedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());

       if (existSchedule != null){
           schedule.setId(existSchedule.getId());
           schedule.setCreateTime(existSchedule.getCreateTime());
           schedule.setUpdateTime(new Date());
           scheduleRepository.save(schedule);
       }else {
           schedule.setCreateTime(new Date());
           schedule.setUpdateTime(new Date());
           scheduleRepository.save(schedule);
       }

        scheduleRepository.save(schedule);
    }

    //查询排班
    //获取科室分页列表
    @Override
    public Page<Schedule> selectPageSchedule(int page, int limit, String hoscode, String depcode) {
        //设置排序规则
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");

        //设置分页参数
        //第一页 0
        PageRequest pageable = PageRequest.of(page - 1, limit, sort);

        //设置条件
        Schedule schedule = new Schedule();
        schedule.setHoscode(hoscode);//医院编号
        schedule.setDepcode(depcode);//科室编号
        Example<Schedule> example = Example.of(schedule);

        //调用方法分页查询
        Page<Schedule> pageModel = scheduleRepository.findAll(pageable);
        return pageModel;
    }

    //删除排班
    @Override
    public void remove(String hoscode, String hosScheduleId) {
        //hoscode+hosScheduleId
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);

        //判断是否为空
        if (schedule != null){
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    //MongoTemplate聚合操作，分页查询
    //根据医院编号和科室编号查询可以预约日期数据
    @Override
    public Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode) {

        //1、封装条件
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //2、封装聚合条件
        Aggregation aggregation =  Aggregation.newAggregation(
                //条件匹配 根据医院编号+科室编号
                Aggregation.match(criteria),

                //分组 workDate
                //SELECT workDate AS workDate FROM user GROUB BY workDate
                Aggregation.group("workDate").first("workDate").as("workDate")
                        //在分组的基础上进行统计数量
                        .count().as("docCount")
                        //分组的基础上进行求和
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                //设置排序
                Aggregation.sort(Sort.Direction.ASC,"workDate"),

                //设置分页条件
                //当前位置：（当前页-1）*每页记录数
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );

        //调用mongoTemplate聚合查询
        AggregationResults<BookingScheduleRuleVo> aggregateResults = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        //查询得到数据集合
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregateResults.getMappedResults();

        //遍历bookingScheduleRuleVoList集合得到日期，使用工具日期得到相对应的星期
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            //获取每个对象日期
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            //调用工具方法，根据日期返回相对应星期
            //Date --> DateTime
            DateTime dateTime = new DateTime(workDate);
            String dayOfWeek = this.getDayOfWeek(dateTime);

            //返回星期封装到对象中
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

        }

        //查询总记录数
        //聚合查询，去掉分页条件，得到list集合，list集合长度就是总记录数
        Aggregation totalAggregation = Aggregation.newAggregation(
                //根据条件匹配，医院编号+科室编号
                Aggregation.match(criteria),
                //分组 workDate
                //SELECT workDate AS workDate FROM user GROUP BY workDate
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggregate = mongoTemplate.aggregate(totalAggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResultsList = totalAggregate.getMappedResults();
        int totalListSize = mappedResultsList.size();

        //返回数据到map，返回
        HashMap<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleVoList",bookingScheduleRuleVoList);
        result.put("totalListSize",totalListSize);

        //获取医院名称
        Hospital hosp = hospitalService.getHosp(hoscode);
        //其他基础数据
        Map<String,String> baseMap = new HashMap<>();
        baseMap.put("hosname",hosp.getHosname());
        result.put("baseMap",baseMap);
        return result;
    }

    //根据医院编号+科室编号+工作日期，查询科室中医生排班详细信息
    @Override
    public List<Schedule> getScheduleDataDetail(String hoscode, String depcode, String workDate) {
        //因为workDate是Date类型，转换类型为DateTime类型
        List<Schedule> list = scheduleRepository.getScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode, new DateTime(workDate));
        return list;
    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
}









