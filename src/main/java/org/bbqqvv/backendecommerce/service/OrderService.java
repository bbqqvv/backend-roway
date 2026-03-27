package org.bbqqvv.backendecommerce.service;

import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.OrderRequest;
import org.bbqqvv.backendecommerce.dto.response.OrderResponse;
import org.springframework.data.domain.Pageable;

import org.bbqqvv.backendecommerce.dto.request.RefundRequest;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest orderRequest);
    PageResponse<OrderResponse> getOrdersByUser(Pageable pageable);
    PageResponse<OrderResponse> getAllOrders(Pageable pageable);
    OrderResponse updateOrder(Long orderId, OrderRequest orderRequest);
    OrderResponse updateOrderStatus(Long orderId, String status);
    void cancelOrder(Long orderId);
    void deleteOrder(Long orderId);
    OrderResponse getOrderByCode(String orderCode);
    OrderResponse getOrderById(Long orderId);

    void requestRefund(Long orderId, RefundRequest request, List<MultipartFile> images);
    void confirmDelivery(Long orderId);
    boolean isProductDeliveredToCurrentUser(Long productId);

    // TEST ONLY
    void fastForwardShippedOrders();
}
