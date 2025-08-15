package org.example.order_demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@TableName("order_local_message")
public class OrderLocalMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 0 待投递到MQ
     * 1 已投递到MQ
     * 2 投递MQ失败
     * 3 库存服务回调订单服务
     * 4 因为库存问题不允许出库 需要取消订单
     * 消息状态 0 待发送 1 已发送 2 发送失败 3 已确认 4 出库失败 取消订单
     */
    private Integer status;

    /**
     * 消息唯一id
     */
    private Long messageId;
    /**
     * 订单id
     */
    private Long bizId;

    /**
     * 消息内容。重试投递使用
     */
    private String payload;
    /**
     * 发送次数
     */
    private Integer count;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 链路id
     */
    private Long traceId;
}
