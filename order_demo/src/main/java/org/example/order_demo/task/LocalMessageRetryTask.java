package org.example.order_demo.task;

import lombok.extern.slf4j.Slf4j;
import org.example.order_demo.entity.OrderLocalMessage;
import org.example.order_demo.service.OrderLocalMessageService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息重试任务
 * 每隔 30 秒扫描一次待重试消息
 */
@Component
@Slf4j
public class LocalMessageRetryTask {

    private final OrderLocalMessageService messageService;

    public LocalMessageRetryTask(OrderLocalMessageService messageService) {
        this.messageService = messageService;
    }

    @Scheduled(fixedDelay = 30_000) // 每30秒执行一次
    public void retryFailedMessages() {
        LocalDateTime now = LocalDateTime.now();
        List<OrderLocalMessage> messages = messageService.listRetryMessages(now);

        for (OrderLocalMessage message : messages) {
            try {
                // TODO: 调用 MQ 发送逻辑，如 rabbitTemplate.convertAndSend(...)
                boolean success = sendToMQ(message);

                if (success) {
                    messageService.markAsSent(message.getMessageId());
                    log.info("消息重试成功，messageId={}", message.getMessageId());
                } else {
                    messageService.incrementCount(message.getMessageId());
                    // 设置下次重试时间（如：指数退避）
                    LocalDateTime nextRetry = now.plusMinutes(1 << message.getCount());
                    messageService.markAsFailedAndRetry(message.getMessageId(), nextRetry);
                }
            } catch (Exception e) {
                log.error("重试消息失败，messageId={}", message.getMessageId(), e);
                // 可以记录错误日志或告警
            }
        }
    }

    private boolean sendToMQ(OrderLocalMessage message) {
        // 实际发送到 RabbitMQ/Kafka 的逻辑
        // return rabbitTemplate...;
        return Math.random() > 0.5; // 模拟成功/失败
    }
}