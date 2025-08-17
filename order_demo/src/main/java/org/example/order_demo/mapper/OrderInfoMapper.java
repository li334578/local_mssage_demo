package org.example.order_demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.order_demo.entity.OrderInfo;

/**
 * 订单信息 Mapper 接口
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    // MyBatis-Plus 自动提供 CRUD 方法
    // 如需自定义 SQL，可在此添加 @Select、@Update 等注解方法

}