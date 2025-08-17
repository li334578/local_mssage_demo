package org.example.order_demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.order_demo.entity.OrderLocalMessage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单本地消息 Service 接口
 */
public interface OrderLocalMessageService extends IService<OrderLocalMessage> {

    /**
     * 创建本地消息（待投递）
     * @param bizId 业务ID（如订单ID）
     * @param messageId 消息唯一ID
     * @param payload 消息内容（JSON字符串）
     * @param traceId 链路ID
     * @return 是否创建成功
     */
    boolean createMessage(Long bizId, Long messageId, String payload, Long traceId);

    /**
     * 标记消息为“已发送”
     * @param messageId 消息ID
     * @return 是否更新成功
     */
    boolean markAsSent(Long messageId);

    /**
     * 标记消息为“发送失败”，并设置下次重试时间
     * @param messageId 消息ID
     * @param nextRetryTime 下次重试时间
     * @return 是否更新成功
     */
    boolean markAsFailedAndRetry(Long messageId, LocalDateTime nextRetryTime);

    /**
     * 增加投递次数
     * @param messageId 消息ID
     * @return 新的投递次数
     */
    int incrementCount(Long messageId);

    /**
     * 标记消息为“已确认”（库存服务回调成功）
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean markAsConfirmed(Long messageId);

    /**
     * 标记消息为“出库失败，需取消订单”
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean markAsStockFailed(Long messageId);

    /**
     * 查询需要重试的消息（状态=2 且 下次重试时间 <= 当前时间）
     * @param now 当前时间
     * @return 消息列表
     */
    List<OrderLocalMessage> listRetryMessages(LocalDateTime now);

    /**
     * 根据业务ID（如订单ID）查询消息
     * @param bizId 业务ID
     * @return 消息对象
     */
    OrderLocalMessage getByBizId(Long bizId);
}