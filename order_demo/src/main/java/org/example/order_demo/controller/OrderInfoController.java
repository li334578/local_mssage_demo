package org.example.order_demo.controller;

import jakarta.annotation.Resource;
import org.example.order_demo.controller.dto.CreateOrderRequest;
import org.example.order_demo.entity.OrderInfo;
import org.example.order_demo.service.OrderInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderInfoController {

    @Resource
    private OrderInfoService orderInfoService;

    @PostMapping
    public boolean create(@RequestBody OrderInfo orderInfo) {
        return orderInfoService.createOrder(orderInfo);
    }

    @GetMapping("/{id}")
    public OrderInfo getById(@PathVariable Long id) {
        return orderInfoService.getById(id);
    }

    @GetMapping("/number/{orderNumber}")
    public OrderInfo getByOrderNumber(@PathVariable String orderNumber) {
        return orderInfoService.getByOrderNumber(orderNumber);
    }

    @GetMapping("/user/{userId}")
    public List<OrderInfo> getByUserId(@PathVariable Long userId) {
        return orderInfoService.listByUserId(userId);
    }

    @PutMapping("/{id}/status")
    public boolean updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return orderInfoService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return orderInfoService.removeById(id);
    }

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody CreateOrderRequest req) {
        Long orderId = orderInfoService.createOrder(
                req.getUserId(), req.getUserName(),
                req.getProductId(), req.getProductName(),
                req.getProductNum(), req.getProductPrice()
        );
        return ResponseEntity.ok(orderId);
    }
}