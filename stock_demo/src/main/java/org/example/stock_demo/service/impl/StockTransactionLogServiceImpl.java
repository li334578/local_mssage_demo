package org.example.stock_demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.stock_demo.entity.StockTransactionLog;
import org.example.stock_demo.mapper.StockTransactionLogMapper;
import org.example.stock_demo.service.StockTransactionLogService;
import org.springframework.stereotype.Service;

@Service
public class StockTransactionLogServiceImpl extends ServiceImpl<StockTransactionLogMapper, StockTransactionLog> implements StockTransactionLogService {
    @Override
    public boolean isMessageProcessed(String messageId) {
        if (messageId == null) return true; // null 视为已处理，防止空消息重复消费

        LambdaQueryWrapper<StockTransactionLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockTransactionLog::getMessageId, messageId)
                .eq(StockTransactionLog::getStatus, 1); // 只查 status=1（成功）的记录

        return this.count(wrapper) > 0;
    }
}
