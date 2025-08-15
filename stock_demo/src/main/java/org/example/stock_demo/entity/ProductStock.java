package org.example.stock_demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductStock {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String productName;
    /**
     * 总库存
     */
    private Integer totalStock;
    /**
     * 可用库存
     */
    private Integer availableStock;
    /**
     * 锁定库存
     */
    private Integer lockStock;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
