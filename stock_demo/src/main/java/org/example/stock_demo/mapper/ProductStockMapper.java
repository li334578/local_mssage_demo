package org.example.stock_demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.stock_demo.entity.ProductStock;

@Mapper
public interface ProductStockMapper extends BaseMapper<ProductStock> {

    /**
     * 扣减可用库存（乐观锁）
     * @param productId 商品ID
     * @param count 扣减数量
     * @param expectedAvailable 当前可用库存（用于乐观锁）
     * @return 影响行数（1=成功，0=失败）
     */
    int deductStock(
            @Param("productId") Long productId,
            @Param("count") Integer count,
            @Param("expectedAvailable") Integer expectedAvailable
    );
}
