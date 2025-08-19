package org.example.stock_demo.consumer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.example.stock_demo.service.ProductStockService;
import org.example.stock_demo.service.StockTransactionLogService;
import org.springframework.stereotype.Component;

/**
 * 库存服务消费者：处理“扣减库存”消息
 * 接收来自订单服务的扣库存请求，执行本地扣减，并回调订单状态
 */
@Component
@RocketMQMessageListener(
        topic = "stock_deduct_topic",
        selectorExpression = "DeductStockTag",  // 可根据 tag 过滤
        consumerGroup = "stock-consumer-group"  // 消费组名（建议按服务命名）
)
@Slf4j
public class StockDeductConsumer implements RocketMQListener<String> {

    @Resource
    private ProductStockService productStockService;

    @Resource
    private OrderCallbackService orderCallbackService;

    @Override
    public void onMessage(String message) {
        log.info("收到扣减库存消息: {}", message);

        try {
            // 1. 解析消息
            JSONObject json = JSON.parseObject(message);
            Long orderId = json.getLong("orderId");
            Long productId = json.getLong("productId");
            Integer productNum = json.getIntValue("productNum");
            String messageId = json.getString("messageId"); // 必须传递！用于幂等

            if (orderId == null || productId == null || productNum == null || messageId == null) {
                log.warn("消息字段缺失，无法处理: {}", message);
                return; // 直接跳过，不重试（属于脏数据）
            }

            // 2. 执行扣库存（内部包含幂等判断）
            boolean deductSuccess = productStockService.deduct(productId, productNum, messageId, String.valueOf(orderId));

            if (deductSuccess) {
                // 3. 扣减成功 → 回调订单：确认出库
                orderCallbackService.confirmOrder(String.valueOf(orderId));
                log.info("库存扣减成功并回调确认，orderId={}", orderId);
            } else {
                // 4. 扣减失败（如库存不足）→ 回调取消订单
                orderCallbackService.cancelOrder(String.valueOf(orderId), "库存不足或扣减失败");
                log.warn("库存扣减失败，已触发取消订单，orderId={}", orderId);
            }

        } catch (Exception e) {
            log.error("处理扣库存消息时发生异常，message={}", message, e);
            // 🔥 抛出异常，触发 RocketMQ 自动重试机制
            throw e;
        }
    }
}