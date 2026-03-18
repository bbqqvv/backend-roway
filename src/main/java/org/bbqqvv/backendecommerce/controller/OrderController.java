package org.bbqqvv.backendecommerce.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.OrderRequest;
import org.bbqqvv.backendecommerce.dto.response.OrderResponse;
import org.bbqqvv.backendecommerce.service.OrderService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Quản lý đơn hàng")
public class OrderController {
    private final OrderService orderService;

    // 📌 Tạo đơn hàng mới
    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody @Valid OrderRequest orderRequest) {
        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order created successfully")
                .data(orderService.createOrder(orderRequest))
                .build();
    }

    // 📌 Lấy thông tin đơn hàng theo ID
    @GetMapping("/{orderCode}")
    public ApiResponse<OrderResponse> getOrderByCode(@PathVariable String orderCode) {
        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order details retrieved successfully")
                .data(orderService.getOrderByCode(orderCode))
                .build();
    }


    // 📌 Lấy danh sách đơn hàng của chính người dùng (không cần userId)
    @GetMapping("/me")
    public ApiResponse<PageResponse<OrderResponse>> getMyOrders(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        PageResponse<OrderResponse> orderPage = orderService.getOrdersByUser(pageable);
        return ApiResponse.<PageResponse<OrderResponse>>builder()
                .success(true)
                .message("User's orders retrieved successfully")
                .data(orderPage)
                .build();
    }
    // 📌 Thêm vào OrderController.java
    @GetMapping("/check-delivery/{productId}")
    public ApiResponse<Boolean> checkProductDelivery(
            @PathVariable Long productId) {
        boolean isDelivered = orderService.isProductDeliveredToCurrentUser(productId);
        return ApiResponse.<Boolean>builder()
                .success(true)
                .message("Product delivery status checked successfully")
                .data(isDelivered)
                .build();
    }

    // 📌 Lấy tất cả đơn hàng (dành cho admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<OrderResponse>> getAllOrders(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        PageResponse<OrderResponse> orderPage = orderService.getAllOrders(pageable);
        return ApiResponse.<PageResponse<OrderResponse>>builder()
                .success(true)
                .message("All orders retrieved successfully")
                .data(orderPage)
                .build();
    }
    // 📌 Cập nhật đơn hàng
    @PutMapping("/{id}")
    public ApiResponse<OrderResponse> updateOrder(@PathVariable Long id, @RequestBody @Valid OrderRequest orderRequest) {
        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order updated successfully")
                .data(orderService.updateOrder(id, orderRequest))
                .build();
    }

    // 📌 Cập nhật trạng thái đơn hàng
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponse> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order status updated successfully")
                .data(orderService.updateOrderStatus(id, status))
                .build();
    }

    // 📌 Hủy đơn hàng theo ID
    @DeleteMapping("/{id}")
    public ApiResponse<String> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ApiResponse.<String>builder()
                .success(true)
                .message("Order has been canceled successfully")
                .data("Order canceled")
                .build();
    }

    // 📌 Xóa đơn hàng theo ID
    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ApiResponse.<String>builder()
                .success(true)
                .message("Order has been deleted successfully")
                .data("Order deleted")
                .build();
    }
}
