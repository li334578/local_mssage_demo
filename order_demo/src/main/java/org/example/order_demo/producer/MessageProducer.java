package com.example.demo.mq;

import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class MessageProducer {

    @Value("${rocketmq.name-server}")
    private String nameServer; // Proxy 地址，如 127.0.0.1:8081

    private Producer producer;

    @PostConstruct
    public void init() throws ClientException {
        // 1. 创建客户端配置
        ClientConfiguration config = ClientConfiguration.newBuilder()
                .setEndpoints(nameServer)
                .build();

        // 2. 获取服务提供者（关键：不是 ClientFactory）
        producer = ClientServiceProvider
                .getClientProvider()
                .newProducerBuilder()
                .setClientConfiguration(config)
                .setProducerGroup("order-producer-group")
                .build();
    }

    /**
     * 发送消息
     */
    public boolean sendMessage(String topic, String body, Long messageId) {
        try {
            // ✅ 正确方式：MessageBuilder.getInstance()
            MessageBuilder messageBuilder = MessageBuilder.getInstance();
            messageBuilder.setTopic(topic);
            messageBuilder.setBody(body.getBytes(StandardCharsets.UTF_8));

            if (messageId != null) {
                messageBuilder.setKeys(String.valueOf(messageId));
            }

            Message message = messageBuilder.build();

            // 发送
            SendReceipt sendReceipt = producer.send(message);
            return sendReceipt != null;
        } catch (ClientException e) {
            System.err.println("发送消息失败: " + e.getMessage());
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (producer != null) {
            try {
                producer.close();
            } catch (ClientException e) {
                System.err.println("关闭生产者失败: " + e.getMessage());
            }
        }
    }
}