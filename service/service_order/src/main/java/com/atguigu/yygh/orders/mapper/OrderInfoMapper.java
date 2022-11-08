package com.atguigu.yygh.orders.mapper;


import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderCountVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author atguigu
 * @since 2022-11-04
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    //4、查询统计数据
    List<OrderCountVo> selectOrderCount(OrderCountQueryVo orderCountQueryVo);
}
