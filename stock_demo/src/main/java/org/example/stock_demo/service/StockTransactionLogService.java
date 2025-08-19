package org.example.stock_demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.stock_demo.entity.StockTransactionLog;

public interface StockTransactionLogService extends IService<StockTransactionLog> {
    boolean isMessageProcessed(String messageId);

}
