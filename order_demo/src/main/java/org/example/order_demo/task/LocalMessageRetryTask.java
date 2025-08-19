package org.example.order_demo.task;

import apache.rocketmq.v2.Message;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.example.order_demo.entity.OrderLocalMessage;
import org.example.order_demo.producer.MessageProducer;
import org.example.order_demo.service.OrderLocalMessageService;
import org.springframework.messaging.support.MessageBuilder;
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
    final int MAX_RETRY = 6; // 最多重试6次
    @Resource
    private MessageProducer messageProducer;

    public LocalMessageRetryTask(OrderLocalMessageService messageService) {
        this.messageService = messageService;
    }

    @Scheduled(fixedDelay = 30_000) // 每30秒执行一次
    public void timedMessages() {
        LocalDateTime now = LocalDateTime.now();
        List<OrderLocalMessage> messages = messageService.listByStatusAndRetryTime(List.of(0, 2), now);

        for (OrderLocalMessage message : messages) {
            try {
                boolean success = sendToMQ(message); // 发送至 MQ
                if (success) {
                    messageService.markAsSent(message.getMessageId());
                    log.info("消息发送成功，messageId={}", message.getMessageId());
                } else {
                    // 发送失败（比如网络问题），进行重试机制
                    int retryCount = message.getCount() + 1;
                    if (retryCount >= MAX_RETRY) {
                        // 🛑 超过最大重试次数，进入终态
                        messageService.markAsDeadLetter(message.getMessageId());
                        log.error("消息发送失败已达最大重试次数，转入死信状态，messageId={}", message.getMessageId());
                        // TODO: 发送告警（钉钉、邮件）
                    } else {
                        // 继续重试
                        messageService.incrementCount(message.getMessageId());
                        LocalDateTime nextRetry = now.plusMinutes(1L << retryCount); // 1,2,4,8,16,32
                        messageService.markAsFailedAndRetry(message.getMessageId(), nextRetry);
                        log.warn("消息发送失败，准备重试，messageId={}, retryCount={}", message.getMessageId(), retryCount);
                    }
                }
            } catch (Exception e) {
                int retryCount = message.getCount() + 1;
                log.error("重试消息时发生异常，messageId={}", message.getMessageId(), e);

                if (retryCount >= MAX_RETRY) {
                    messageService.markAsDeadLetter(message.getMessageId());
                    log.error("异常次数达上限，转入死信状态，messageId={}", message.getMessageId());
                    // 告警
                } else {
                    messageService.incrementCount(message.getMessageId());
                    LocalDateTime nextRetry = now.plusMinutes(1L << retryCount);
                    messageService.markAsFailedAndRetry(message.getMessageId(), nextRetry);
                }
            }
        }
    }

    private boolean sendToMQ(OrderLocalMessage message) {
        String topic = "stock_deduct_topic";           // 主题
        String destination = topic + ":" + "deduct_tag";       // RocketMQ 格式：topic:tags
        String payload = message.getPayload();         // 消息内容（JSON 字符串）
        return messageProducer.sendMessage(destination, payload, message.getMessageId());
    }
}