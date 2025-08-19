package org.example.order_demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.order_demo.entity.OrderLocalMessage;
import org.example.order_demo.mapper.OrderLocalMessageMapper;
import org.example.order_demo.service.OrderLocalMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单本地消息 Service 实现类
 */
@Service
public class OrderLocalMessageServiceImpl 
    extends ServiceImpl<OrderLocalMessageMapper, OrderLocalMessage>
    implements OrderLocalMessageService {

    @Resource
    private OrderLocalMessageMapper orderLocalMessageMapper;

    @Override
    public boolean createMessage(Long bizId, Long messageId, String payload, Long traceId) {
        OrderLocalMessage message = OrderLocalMessage.builder().bizId(bizId).messageId(messageId)
                .payload( payload).traceId(traceId).status(0).count(0).createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now()).build();
        return save(message);
    }

    @Override
    public boolean createMessage(Long bizId, Long messageId, String payload, Long traceId, Integer status, LocalDateTime nextRetryTime) {
        OrderLocalMessage message = OrderLocalMessage.builder().bizId(bizId).messageId(messageId)
                .payload( payload).traceId(traceId).status(status).count(0).createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .nextRetryTime(nextRetryTime).build();
        return save(message);
    }

    @Override
    public boolean markAsSent(Long messageId) {
        return lambdaUpdate()
            .eq(OrderLocalMessage::getMessageId, messageId)
            .set(OrderLocalMessage::getStatus, 1)
            .set(OrderLocalMessage::getUpdateTime, LocalDateTime.now())
            .update();
    }

    @Override
    public boolean markAsFailedAndRetry(Long messageId, LocalDateTime nextRetryTime) {
        return lambdaUpdate()
            .eq(OrderLocalMessage::getMessageId, messageId)
            .set(OrderLocalMessage::getStatus, 2)
            .set(OrderLocalMessage::getNextRetryTime, nextRetryTime)
            .set(OrderLocalMessage::getUpdateTime, LocalDateTime.now())
            .update();
    }

    @Override
    public int incrementCount(Long messageId) {
        return orderLocalMessageMapper.incrementCount(messageId); // 需要在 Mapper.xml 中实现，或使用乐观更新
    }

    @Override
    public boolean markAsConfirmed(Long messageId) {
        return lambdaUpdate()
            .eq(OrderLocalMessage::getMessageId, messageId)
            .set(OrderLocalMessage::getStatus, 3)
            .set(OrderLocalMessage::getUpdateTime, LocalDateTime.now())
            .update();
    }

    @Override
    public boolean markAsStockFailed(Long messageId) {
        return lambdaUpdate()
            .eq(OrderLocalMessage::getMessageId, messageId)
            .set(OrderLocalMessage::getStatus, 4)
            .set(OrderLocalMessage::getUpdateTime, LocalDateTime.now())
            .update();
    }

    @Override
    public List<OrderLocalMessage> listRetryMessages(LocalDateTime now) {
        return lambdaQuery()
            .eq(OrderLocalMessage::getStatus, 2) // 发送失败
            .le(OrderLocalMessage::getNextRetryTime, now)
            .list();
    }

    @Override
    public List<OrderLocalMessage> listByStatusAndRetryTime(List<Integer> statusList, LocalDateTime now) {
        return lambdaQuery()
                .in(OrderLocalMessage::getStatus, statusList) // 发送失败
                .le(OrderLocalMessage::getNextRetryTime, now)
                .list();
    }

    @Override
    public OrderLocalMessage getByBizId(Long bizId) {
        return lambdaQuery()
            .eq(OrderLocalMessage::getBizId, bizId)
            .one();
    }

    @Override
    public void markAsDeadLetter(Long messageId) {
        LambdaUpdateWrapper<OrderLocalMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderLocalMessage::getMessageId, messageId)
                .in(OrderLocalMessage::getStatus, 0, 2) // 只更新 PENDING/FAILED
                .set(OrderLocalMessage::getStatus, 5)   // DEAD_LETTER
                .set(OrderLocalMessage::getUpdateTime, LocalDateTime.now());
        update(wrapper);
    }
}