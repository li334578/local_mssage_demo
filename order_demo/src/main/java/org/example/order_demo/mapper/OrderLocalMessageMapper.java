package org.example.order_demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.example.order_demo.entity.OrderLocalMessage;

/**
 * 订单本地消息 Mapper 接口
 * 用于可靠消息最终一致性（本地消息表模式）
 */
@Mapper
public interface OrderLocalMessageMapper extends BaseMapper<OrderLocalMessage> {
    // 可在此添加自定义方法，如复杂查询
    @Update("UPDATE order_local_message SET count = count + 1, update_time = NOW() WHERE message_id = #{messageId}")
    int incrementCount(@Param("messageId") Long messageId);
}