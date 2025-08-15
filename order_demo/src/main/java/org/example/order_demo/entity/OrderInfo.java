package org.example.order_demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 订单编号
     */
    private String orderNumber;
    /**
     * 下单用户id
     */
    private Long userId;
    /**
     * 下单用户名称
     */
    private String userName;

    /**
     * 商品id
     */
    private Long productId;
    /**
     * 商品名称
     */
    private String productName;
    /**
     * 商品数量
     */
    private Integer productNum;
    /**
     * 商品单价
     */
    private BigDecimal productPrice;
    /**
     * 订单总价
     */
    private BigDecimal totalPrice;
    /**
     * 订单状态 0=待支付, 1=支付中, 2=已支付, 3=已发货, 4=已完成, 5=已取消,6=已关闭
     */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String remark;
    private Long traceId;
}
