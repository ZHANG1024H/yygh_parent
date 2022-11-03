package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    //医院名称可以从总获取
    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

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

    //显示科室可以预约日期数据
    //医院编号+科室编号+分页参数
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {

        //1、获取所有显示日期，根据当前日期+预约周期
        //根据医院编号获取预约信息
        Hospital hospital = hospitalService.getHosp(hoscode);
        //获取预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        //根据当前日期+预约周期把所有显示的日期得到 (返回IPage为了分页方便）
        IPage iPage = this.getListDate(page,limit,bookingRule);
        List<Date> dateList = iPage.getRecords();

        //2、根据医院编号+科室编号+所有日期进行查询
        //MongoTemplate聚合查询
        //根据workDate进行分组
        //封装查询的条件
        Criteria criteria = Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode)
                .and("workDate").in(dateList);
        //封装聚合条件
        Aggregation agg = Aggregation.newAggregation(
                //条件的匹配
                Aggregation.match(criteria),
                //根据workDate进行分组
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        //分组基础之上 统计 求和
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        //调用mongoTemplate方法
        AggregationResults<BookingScheduleRuleVo> aggregateResult =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleRuleVoList = aggregateResult.getMappedResults();

        //3、查看所有日期，查看每个日期中是否有号
        //判断显示每个日期在mongodb查询数据是否存在，存在设置有号，不存在设置无号
        //scheduleRuleVoList --> map<日期,日期对应的数据>
        Map<Date, BookingScheduleRuleVo> scheduleRuleVoMap = scheduleRuleVoList.stream()
                .collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate,
                        BookingScheduleRuleVo -> BookingScheduleRuleVo));

        //根据日期查询map中的key
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        //dateList每页所有日期，遍历，得到每个日期
        int len = dateList.size();
        for (int i = 0; i < len; i++) {
            Date date = dateList.get(i);
            //拿着每个日期到map查询，map的key就是日期
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleRuleVoMap.get(date);
            if (bookingScheduleRuleVo == null){//无号
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数，-1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            //有号
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一个日期，显示即将放号 状态 0：正常 1：即将放号 -1：当天已停止放号
            //最后一页的最后一条记录
            if (i == len-1 && page == iPage.getPages()){
                bookingScheduleRuleVo.setStatus(1);
            }else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //第一个日期，如果当前日期时间过了停止挂号时间，显示停止挂号 -1
            if (i == 0 && page ==1){
                DateTime stopTime = this.getDateTime(new Date(),bookingRule.getStopTime());
                if (stopTime.isBeforeNow()){
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }

            //放到最终list集合
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        //封装map集合返回
        //创建Map集合，放最终数据
        HashMap<String, Object> result = new HashMap<>();
        //可预约日期规则数据
        result.put("bookingScheduleList",bookingScheduleRuleVoList);
        result.put("total",iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHosp(hoscode).getHosname());
        //科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);

        //可预约日期规则数据
        return result;
    }

    //根据当前日期+预约周期，获取所有显示的日期（做分页显示）
    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //使用日期工具类
        //逻辑处理
        //releaseTime:放号时间
        //的到了目前（年月日小时分秒）放号时间
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());

        //预约周期
        Integer cycle = bookingRule.getCycle();

        //判断当前日期时间是否过了放号时间，如果过了放号时间，预约周期加一
        //isBeforeNow：是否在当前事件之前
        if (releaseTime.isBeforeNow()){
            cycle += 1;
        }
        //获取显示所有日期  根据当前日期+预约周期
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            //让当前日期做 + 操作
            DateTime currenDateTime = new DateTime().plusDays(i);
            //把DateTime转换成Date，因为list中是Date类型
            String dateString = currenDateTime.toString("yyyy-MM-dd");
            Date date = new DateTime(dateString).toDate();
            //放到集合中
            dateList.add(date);
        }
        //所有日期做分页处理
        //传入第几页（数字）返回第几页的数据
        //所有日期分页处理，每次返回每页数据
        //dateList 集合存储所有显示日期
        List<Date> pageDateList = new ArrayList<>();
        //当前页减一乘以每页记录数
        //得到开始位置 1
        int start = (page-1)*limit;//1
        //每页显示3
        int end =(page-1)*limit+limit;//3
        if (end>dateList.size()) end = dateList.size();
        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }
        //使用IPage封装分页所有数据
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit, dateList.size());
        iPage.setRecords(pageDateList);
        return iPage;


    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
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

    //6、根据排班id获取排班的详情数据
    @Override
    public Schedule getScheduleId(String id) {

        Schedule schedule = this.packageSchedule(scheduleRepository.findById(id).get());
        return schedule;
    }
    //6.1 获取医院名称和科室名称
    private Schedule packageSchedule(Schedule schedule) {
        String hoscode = schedule.getHoscode();
        String depcode = schedule.getDepcode();

        //医院名称
        Hospital hosp = hospitalService.getHosp(hoscode);
        schedule.getParam().put("hosname",hosp.getHosname());
        //科室名称
        Department department = departmentService.getDepartment(hoscode, depcode);
        schedule.getParam().put("depname", department.getDepname());
        return schedule;
    }

}









