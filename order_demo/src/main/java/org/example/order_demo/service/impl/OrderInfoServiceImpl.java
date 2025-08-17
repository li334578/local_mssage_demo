package org.example.order_demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.order_demo.entity.OrderInfo;
import org.example.order_demo.entity.OrderLocalMessage;
import org.example.order_demo.mapper.OrderInfoMapper;
import org.example.order_demo.service.OrderInfoService;
import org.example.order_demo.service.OrderLocalMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单信息 Service 实现类
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Resource
    private final OrderLocalMessageService messageService;
    @Resource
    private final MessageProducer messageProducer; // MQ 发送组件

    @Override
    public OrderInfo getByOrderNumber(String orderNumber) {
        return lambdaQuery().eq(OrderInfo::getOrderNumber, orderNumber).one();
    }

    @Override
    public List<OrderInfo> listByUserId(Long userId) {
        return lambdaQuery().eq(OrderInfo::getUserId, userId)
                           .orderByDesc(OrderInfo::getCreateTime)
                           .list();
    }

    @Override
    public boolean createOrder(OrderInfo orderInfo) {
        // 设置默认值
        orderInfo.setCreateTime(LocalDateTime.now());
        orderInfo.setUpdateTime(LocalDateTime.now());
        if (orderInfo.getStatus() == null) {
            orderInfo.setStatus(0); // 默认待支付
        }
        return save(orderInfo);
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        OrderInfo order = OrderInfo.builder().id( id).status(status).updateTime(LocalDateTime.now()).build();
        return updateById(order);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(
            Long userId,
            String userName,
            Long productId,
            String productName,
            Integer productNum,
            BigDecimal productPrice) {

        // 1. 创建订单
        OrderInfo order = OrderInfo.builder().userId(userId).userName(userName)
                .productId(productId).productName(productName).productPrice(productPrice)
                .totalPrice(productPrice.multiply(BigDecimal.valueOf(productNum)))
                .status(0)
                .updateTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();
        baseMapper.insert(order); // 使用父类 baseMapper

        // 2. 生成本地消息
        Long messageId = IdGenerator.nextId();
        String payload = buildDeductStockMessage(order);

        messageService.createMessage(order.getId(), messageId, payload, order.getTraceId());

        // 3. 第一次尝试发送MQ
        boolean sent = messageProducer.sendMessage("stock.deduct.queue", payload, messageId);
        if (sent) {
            messageService.markAsSent(messageId);
        } else {
            LocalDateTime nextRetry = LocalDateTime.now().plusSeconds(30);
            messageService.markAsFailedAndRetry(messageId, nextRetry);
        }

        return order.getId();
    }

    @Override
    @Transactional
    public void payOrder(Long orderId) {
        OrderInfo order = baseMapper.selectById(orderId);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        if (order.getStatus() != 0)
            throw new IllegalStateException("订单状态不可支付");

        order.setStatus(1); // 支付中
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    @Transactional
    public void onPaymentSuccess(Long orderId) {
        OrderInfo order = baseMapper.selectById(orderId);
        if (order == null || order.getStatus() != 1)
            throw new IllegalArgumentException("订单状态异常");

        order.setStatus(2); // 已支付
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    @Transactional
    public void onStockDeductSuccess(Long orderId) {
        OrderInfo order = baseMapper.selectById(orderId);
        if (order == null || order.getStatus() >= 2) return;

        // 幂等处理
        OrderLocalMessage message = messageService.getByBizId(orderId);
        if (message != null && message.getStatus() == 1) {
            messageService.markAsConfirmed(message.getMessageId());
        }
    }

    @Override
    @Transactional
    public void onStockDeductFailed(Long orderId) {
        OrderInfo order = baseMapper.selectById(orderId);
        if (order == null || order.getStatus() >= 2) return;

        order.setStatus(6);
        order.setRemark("库存不足，订单关闭");
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);

        OrderLocalMessage message = messageService.getByBizId(orderId);
        if (message != null) {
            messageService.markAsStockFailed(message.getMessageId());
        }

        notifyUserStockFailed(order.getUserId(), orderId);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        OrderInfo order = baseMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId))
            throw new IllegalArgumentException("订单不存在或无权限");
        if (order.getStatus() != 0)
            throw new IllegalStateException("订单无法取消");

        order.setStatus(5); // 已取消
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    @Transactional
    public void closeExpiredOrder(Long orderId) {
        OrderInfo order = baseMapper.selectById(orderId);
        if (order == null || order.getStatus() != 0) return;

        order.setStatus(6);
        order.setRemark("超时未支付，自动关闭");
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);

        // 发送释放库存消息
        messageProducer.sendMessage("stock.release.queue", buildReleaseStockMessage(order));
    }

    @Override
    @Transactional
    public void deliverOrder(Long orderId) {
        OrderInfo order = baseMapper.selectById(orderId);
        if (order == null || order.getStatus() != 2)
            throw new IllegalArgumentException("订单不能发货");

        order.setStatus(3); // 已发货
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId) {
        OrderInfo order = baseMapper.selectById(orderId);
        if (order == null || order.getStatus() != 3)
            throw new IllegalArgumentException("订单不能完成");

        order.setStatus(4); // 已完成
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);

        // 可触发后续流程：积分、优惠券等
    }

    // ======= 工具方法 =======

    private String buildDeductStockMessage(OrderInfo order) {
        return String.format(
                "{\"orderId\":%d,\"productId\":%d,\"productNum\":%d,\"messageId\":%d}",
                order.getId(), order.getProductId(), order.getProductNum(), IdGenerator.nextId()
        );
    }

    private String buildReleaseStockMessage(OrderInfo order) {
        return String.format(
                "{\"orderId\":%d,\"productId\":%d,\"productNum\":%d}",
                order.getId(), order.getProductId(), order.getProductNum()
        );
    }

    private void notifyUserStockFailed(Long userId, Long orderId) {
        // TODO: 发送通知
    }
}