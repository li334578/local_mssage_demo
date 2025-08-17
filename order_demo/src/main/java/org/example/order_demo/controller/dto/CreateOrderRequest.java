package org.example.order_demo.controller.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 创建订单请求参数
 */
@Data
public class CreateOrderRequest {

    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private Integer productNum;
    private BigDecimal productPrice;

    // 可选：traceId，用于链路追踪
    private Long traceId;
}