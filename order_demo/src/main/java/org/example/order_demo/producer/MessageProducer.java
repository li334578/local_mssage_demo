package org.example.order_demo.producer;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class MessageProducer {

    private static final Logger log = LoggerFactory.getLogger(MessageProducer.class);

    @Value("${rocketmq.name-server}")
    private String nameServer;  // 通常是 9876 端口，如 127.0.0.1:9876

    @Value("${rocketmq.producer.group:order-producer-group}")
    private String producerGroup;

    private DefaultMQProducer producer;

    @PostConstruct
    public void init() {
        try {
            log.info("正在启动 RocketMQ 生产者，NameServer: {}, Group: {}", nameServer, producerGroup);

            producer = new DefaultMQProducer(producerGroup);
            producer.setNamesrvAddr(nameServer);
            producer.start();
            log.info("RocketMQ 生产者启动成功");
        } catch (Exception e) {
            log.error("RocketMQ 生产者启动失败", e);
            throw new RuntimeException("Failed to start RocketMQ producer", e);
        }
    }

    /**
     * 发送消息
     */
    public boolean sendMessage(String topic, String body, Long messageId) {
        return sendMessage(topic, body, messageId, null);
    }

    /**
     * 发送消息（支持 Tag）
     */
    public boolean sendMessage(String topic, String body, Long messageId, String tag) {
        if (producer == null) {
            log.warn("生产者未启动，无法发送消息");
            return false;
        }

        try {
            Message msg = new Message(
                    topic,
                    tag != null ? tag : "",
                    messageId != null ? String.valueOf(messageId) : null,
                    body.getBytes(RemotingHelper.DEFAULT_CHARSET)
            );

            SendResult sendResult = producer.send(msg);
            log.debug("消息发送成功: {}, Topic: {}, MsgId: {}", sendResult.getSendStatus(), topic, sendResult.getMsgId());
            return true;
        } catch (Exception e) {
            log.error("发送消息失败，Topic: {}", topic, e);
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (producer != null) {
            try {
                producer.shutdown();
                log.info("RocketMQ 生产者已关闭");
            } catch (Exception e) {
                log.error("关闭生产者失败", e);
            }
        }
    }
}