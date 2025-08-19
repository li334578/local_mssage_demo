package org.example.stock_demo.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.stock_demo.entity.ProductStock;
import org.example.stock_demo.entity.StockTransactionLog;
import org.example.stock_demo.mapper.ProductStockMapper;
import org.example.stock_demo.service.ProductStockService;
import org.example.stock_demo.service.StockTransactionLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ProductStockServiceImpl extends ServiceImpl<ProductStockMapper, ProductStock> implements ProductStockService {

    @Resource
    private ProductStockMapper productStockMapper;

    @Resource
    private StockTransactionLogService stockTransactionLogService;

    @Override
    public boolean deduct(Long productId, Integer count, String messageId, String orderId) {
        if (count == null || count <= 0) {
            log.warn("扣减库存数量非法，productId={}, count={}", productId, count);
            return false;
        }

        // 1. 幂等性校验：通过 messageId 判断是否已处理
        if (stockTransactionLogService.isMessageProcessed(messageId)) {
            log.info("消息已处理，幂等性拦截，messageId={}", messageId);
            return true; // 已成功处理过，返回 true 避免重复扣减
        }

        // 2. 查询商品库存（带乐观锁 version 或 update_time 控制）
        ProductStock stock = productStockMapper.selectOne(Wrappers.lambdaQuery(ProductStock.class).eq(ProductStock::getProductId, productId));
        if (stock == null) {
            log.warn("商品库存不存在，productId={}", productId);
            saveFailedLog(messageId, orderId, productId, count, "商品库存不存在");
            return false;
        }

        // 3. 检查可用库存是否足够
        if (stock.getAvailableStock() < count) {
            log.warn("库存不足，productId={}, available={}, required={}", productId, stock.getAvailableStock(), count);
            saveFailedLog(messageId, orderId, productId, count, "库存不足");
            return false;
        }

        // 4. 扣减库存：available -= count
        int updated = productStockMapper.deductStock(
                productId,
                count,
                stock.getAvailableStock() // 用于乐观锁（where available_stock = ?）
        );

        if (updated == 0) {
            log.warn("扣库存失败（乐观锁冲突），可能并发超卖，productId={}, count={}", productId, count);
            saveFailedLog(messageId, orderId, productId, count, "并发冲突，扣减失败");
            throw new RuntimeException("库存扣减失败，请重试");
        }

        // 5. 记录成功日志
        saveSuccessLog(messageId, orderId, productId, count, stock, "扣减库存");

        log.info("库存扣减成功，productId={}, count={}", productId, count);
        return true;
    }

    private void saveSuccessLog(String messageId, String orderId, Long productId, Integer count,
                                ProductStock beforeStock, String reason) {
        ProductStock afterStock = productStockMapper.selectByProductId(productId);

        StockTransactionLog log = StockTransactionLog.builder()
                .bizType("DEDUCT")
                .bizId(orderId)
                .messageId(messageId)
                .productId(productId)
                .changeType(3) // DEDUCT
                .changeAmount(count)
                .reason(reason)
                .stockBefore(objectToJson(beforeStock))
                .stockAfter(objectToJson(afterStock))
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        stockTransactionLogService.save(log);
    }

    private void saveFailedLog(String messageId, String orderId, Long productId, Integer count, String errorMsg) {
        ProductStock beforeStock = productStockMapper.selectById(productId);

        StockTransactionLog log = StockTransactionLog.builder()
                .bizType("DEDUCT")
                .bizId(orderId)
                .messageId(messageId)
                .productId(productId)
                .changeType(3)
                .changeAmount(count)
                .reason("扣减失败：" + errorMsg)
                .stockBefore(objectToJson(beforeStock))
                .stockAfter(objectToJson(beforeStock))
                .status(0)
                .errorMessage(errorMsg)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        stockTransactionLogService.save(log);
    }

    private String objectToJson(Object obj) {
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            return "{\"error\": \"序列化失败\"}";
        }
    }
}
