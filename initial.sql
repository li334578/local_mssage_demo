CREATE TABLE `order_info` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `order_number` VARCHAR(64) DEFAULT NULL COMMENT '订单编号',
                              `user_id` BIGINT DEFAULT NULL COMMENT '下单用户id',
                              `user_name` VARCHAR(50) DEFAULT NULL COMMENT '下单用户名称',
                              `product_id` BIGINT DEFAULT NULL COMMENT '商品id',
                              `product_name` VARCHAR(100) DEFAULT NULL COMMENT '商品名称',
                              `product_num` INT DEFAULT NULL COMMENT '商品数量',
                              `product_price` DECIMAL(10,2) DEFAULT NULL COMMENT '商品单价',
                              `total_price` DECIMAL(10,2) DEFAULT NULL COMMENT '订单总价',
                              `status` INT DEFAULT NULL COMMENT '订单状态: 0=待支付, 1=支付中, 2=已支付, 3=已发货, 4=已完成, 5=已取消, 6=已关闭',
                              `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
                              `trace_id` BIGINT DEFAULT NULL COMMENT '链路追踪ID',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_order_number` (`order_number`), -- 可选：订单编号唯一
                              KEY `idx_user_id` (`user_id`),
                              KEY `idx_product_id` (`product_id`),
                              KEY `idx_status` (`status`),
                              KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息表';

CREATE TABLE `product_stock` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `product_id` BIGINT NOT NULL COMMENT '商品ID',
                                 `product_name` VARCHAR(100) DEFAULT NULL COMMENT '商品名称',
                                 `total_stock` INT NOT NULL DEFAULT '0' COMMENT '总库存',
                                 `available_stock` INT NOT NULL DEFAULT '0' COMMENT '可用库存（可销售）',
                                 `lock_stock` INT NOT NULL DEFAULT '0' COMMENT '锁定库存（已下单未支付）',
                                 `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_product_id` (`product_id`), -- 保证每个商品只有一条库存记录
                                 KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品库存表';



CREATE TABLE `order_local_message` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                       `status` INT NOT NULL DEFAULT '0' COMMENT '消息状态: 0=待发送, 1=已发送, 2=发送失败, 3=已确认, 4=出库失败需取消',
                                       `message_id` BIGINT NOT NULL COMMENT '消息唯一ID',
                                       `biz_id` BIGINT NOT NULL COMMENT '业务ID（如订单ID）',
                                       `payload` TEXT COMMENT '消息内容（JSON）',
                                       `count` INT NOT NULL DEFAULT '0' COMMENT '投递次数',
                                       `next_retry_time` DATETIME DEFAULT NULL COMMENT '下次重试时间',
                                       `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `trace_id` BIGINT DEFAULT NULL COMMENT '链路追踪ID',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_message_id` (`message_id`),
                                       KEY `idx_biz_id` (`biz_id`),
                                       KEY `idx_next_retry_time` (`next_retry_time`) COMMENT '用于定时任务扫描待重试消息',
                                       KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单本地消息表（用于可靠消息）';