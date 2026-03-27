package org.bbqqvv.backendecommerce.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.entity.Order;
import org.bbqqvv.backendecommerce.entity.OrderStatus;
import org.bbqqvv.backendecommerce.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tự động xác nhận giao hàng cho các đơn SHIPPED quá 7 ngày
 * (3 ngày giao hàng + 4 ngày chờ user xác nhận)
 * mà user chưa bấm "Đã nhận hàng".
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderAutoConfirmScheduler {

    private final OrderRepository orderRepository;

    // Chạy mỗi giờ
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void autoConfirmDelivery() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(6);
        List<Order> shippedOrders = orderRepository.findShippedOrdersOlderThan(cutoff);

        if (!shippedOrders.isEmpty()) {
            log.info("Auto-confirming delivery for {} orders shipped before {}", shippedOrders.size(), cutoff);
            for (Order order : shippedOrders) {
                order.setStatus(OrderStatus.DELIVERED);
            }
            orderRepository.saveAll(shippedOrders);
            log.info("Auto-confirmed {} orders", shippedOrders.size());
        }
    }
}
