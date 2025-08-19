package org.example.stock_demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.stock_demo.entity.ProductStock;

public interface ProductStockService extends IService<ProductStock> {
    boolean deduct(Long productId, Integer count, String messageId, String orderId);
}
