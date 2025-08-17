package org.example.order_demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.order_demo.entity.OrderInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单信息 Service 接口
 */
public interface OrderInfoService extends IService<OrderInfo> {

    /**
     * 根据订单编号查询订单
     * @param orderNumber 订单编号
     * @return OrderInfo
     */
    OrderInfo getByOrderNumber(String orderNumber);

    /**
     * 根据用户ID查询订单列表
     * @param userId 用户ID
     * @return List<OrderInfo>
     */
    List<OrderInfo> listByUserId(Long userId);

    /**
     * 创建新订单
     * @param orderInfo 订单信息
     * @return 是否成功
     */
    boolean createOrder(OrderInfo orderInfo);

    /**
     * 更新订单状态
     * @param id 订单ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);


    /**
     * 创建订单（写订单 + 写本地消息表 + 发送MQ）
     *
     * @param userId        用户ID
     * @param userName      用户名
     * @param productId     商品ID
     * @param productName   商品名称
     * @param productNum    数量
     * @param productPrice  单价
     * @return 订单ID
     */
    Long createOrder(
            Long userId,
            String userName,
            Long productId,
            String productName,
            Integer productNum,
            BigDecimal productPrice);

    /**
     * 用户发起支付
     * @param orderId 订单ID
     */
    void payOrder(Long orderId);

    /**
     * 支付成功回调
     * @param orderId 订单ID
     */
    void onPaymentSuccess(Long orderId);

    /**
     * 库存服务回调：扣减库存成功
     * @param orderId 订单ID
     */
    void onStockDeductSuccess(Long orderId);

    /**
     * 库存服务回调：扣减库存失败（如库存不足）
     * @param orderId 订单ID
     */
    void onStockDeductFailed(Long orderId);

    /**
     * 用户取消订单（仅限未支付）
     * @param orderId 订单ID
     * @param userId 用户ID（权限校验）
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 关闭超时未支付订单（由定时任务调用）
     * @param orderId 订单ID
     */
    void closeExpiredOrder(Long orderId);

    /**
     * 仓库发货
     * @param orderId 订单ID
     */
    void deliverOrder(Long orderId);

    /**
     * 完成订单（用户确认收货或系统自动完成）
     * @param orderId 订单ID
     */
    void completeOrder(Long orderId);
}