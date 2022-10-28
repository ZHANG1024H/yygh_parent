package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.sun.xml.internal.bind.v2.TODO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    //远程调用使用
    @Autowired
    private DictFeignClient dictFeignClient;

    //添加医院方法
    @Override
    public void saveHosp(Map<String, Object> newObjectMap) {
        //newObjectMap --> Hospital（map转成对象）
        //Json工具实现
        //1、newObjectMap转换成json字符串
        String jsonString = JSONObject.toJSONString(newObjectMap);
        //json字符串转换为Hospital对象
        Hospital hospital = JSONObject.parseObject(jsonString, Hospital.class);

        //2、判断当前医院数据是否已经添加，如果添加，进行修改，没有添加直接添加
        //根据医院编号查询
        Hospital exisHospital = hospitalRepository.findByHoscode(hospital.getHoscode());

        if (exisHospital != null){//已经添加过了，修改
            //设置id值
            hospital.setId(exisHospital.getId());
            hospital.setCreateTime(exisHospital.getCreateTime());
            hospital.setUpdateTime(new Date());

            //调用方法添加
            hospitalRepository.save(hospital);
        }else {//没有添加直接添加
            // 调用方法添加
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }

        //调用方法添加
        hospitalRepository.save(hospital);
    }

    //查询医院信息
    @Override
    public Hospital getHosp(String hoscode) {
        Hospital byHoscode = hospitalRepository.findByHoscode(hoscode);
        return byHoscode;
    }

    //医院条件分页查询
    @Override
    public Page<Hospital> selectPageHosp(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        //设置排序
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");

        //设置分页
        Pageable pageable = PageRequest.of(page-1,limit,sort);

        //封装条件
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        //hospitalQueryVo -- hospital
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);
        Example<Hospital> example = Example.of(hospital, matcher);


        //调用方法得到
        Page<Hospital> pageModel = hospitalRepository.findAll(example, pageable);

        //获取查询list集合
        pageModel.getContent().stream().forEach(item ->{
            //遍历list集合，得到每个hospital对象
            this.packHospital(item);
        });
        return pageModel;
    }

    //更新医院上线状态
    @Override
    public void updateStatus(String id, Integer status) {
        if (status.intValue() == 0 || status.intValue() == 1){
            Hospital hospital = hospitalRepository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }
    }

    //查看医院详情
    @Override
    public Map<String, Object> showHosp(String id) {
        //根据id查询
        Hospital hospital = this.packHospital(hospitalRepository.findById(id).get());
        System.out.println(hospital);
        HashMap<String, Object> result = new HashMap<>();
        //医院基本信息(包含医院等级)
        result.put("hospital",hospital);
        //单独处理更直观
        result.put("bookingRule",hospital.getBookingRule());
        return result;
    }


    //获取每个对象编号，远程调用根据编号获取名称，把获取名称封装hospital对象的map中
    private Hospital packHospital(Hospital hospital) {
        //获取每个对象编号
        String hostype = hospital.getHostype();//医院等级
        //省，市，区
        String provinceCode = hospital.getProvinceCode();
        String cityCode = hospital.getCityCode();
        String districtCode = hospital.getDistrictCode();
        //远程调用根据编号获取对应名称
        String provinceString = dictFeignClient.getName(provinceCode);//省
        String cityString = dictFeignClient.getName(cityCode);//市
        String districtString = dictFeignClient.getName(districtCode);//区
        //医院等级名称
        String hostypeString = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(), hostype);
        //数据封装到map
        hospital.getParam().put("hostypeString",hostypeString);
        //拼接 省+市+区+详细地址
        hospital.getParam().put("fullAddress", provinceString + cityString + districtString + hospital.getAddress());


        return hospital;


    }


}





















