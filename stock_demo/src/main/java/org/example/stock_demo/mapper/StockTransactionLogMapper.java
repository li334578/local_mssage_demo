package org.example.stock_demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.stock_demo.entity.StockTransactionLog;

@Mapper
public interface StockTransactionLogMapper extends BaseMapper<StockTransactionLog> {
}
