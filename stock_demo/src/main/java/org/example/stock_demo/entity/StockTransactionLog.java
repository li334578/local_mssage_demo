package org.example.stock_demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("stock_transaction_log")
public class StockTransactionLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务类型：LOCK, UNLOCK, DEDUCT, RETURN, MANUAL_ADJUST
     */
    private String bizType;

    /**
     * 业务ID，如订单号 ORD123
     */
    private String bizId;

    /**
     * 消息ID，用于幂等控制（来自 local_message.message_id）
     * 唯一索引保证同一条消息只处理一次
     */
    private String messageId;

    /**
     * 商品id
     */
    private Long productId;

    /**
     * 变动类型：
     * 1 = LOCK（锁定）
     * 2 = UNLOCK（释放）
     * 3 = DEDUCT（扣减）
     * 4 = RETURN（归还）
     * 5 = MANUAL_ADJUST（手动调整）
     */
    private Integer changeType;

    /**
     * 变动数量（正数）
     */
    private Integer changeAmount;

    /**
     * 原因说明
     */
    private String reason;

    /**
     * 操作前库存快照
     * JSON: {"total":100,"available":90,"locked":10,"soldCount":0}
     */
    private String stockBefore;

    /**
     * 操作后库存快照
     */
    private String stockAfter;

    /**
     * 状态：1=成功，0=失败
     */
    private Integer status;

    /**
     * 错误信息（失败时记录）
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
