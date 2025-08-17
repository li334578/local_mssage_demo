package org.example.order_demo.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * 全局唯一ID生成器（基于雪花算法）
 * 保证分布式环境下生成不重复的 Long 型ID
 */
public class IdGenerator {

    // 使用 Hutool 的 Snowflake 工具（可自定义 workerId 和 dataCenterId）
    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    /**
     * 生成唯一ID
     * @return Long 类型的唯一ID
     */
    public static Long nextId() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 生成唯一字符串ID（可选）
     * @return 字符串形式的ID
     */
    public static String nextIdStr() {
        return SNOWFLAKE.nextIdStr();
    }
}