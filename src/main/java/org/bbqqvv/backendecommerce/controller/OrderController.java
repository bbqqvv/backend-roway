package org.bbqqvv.backendecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
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

    @PostMapping
    @Operation(summary = "Tạo đơn hàng mới", description = "Tạo một đơn hàng mới từ giỏ hàng hiện tại. Hỗ trợ áp dụng mã giảm giá và tự động trừ kho.")
    public ApiResponse<OrderResponse> createOrder(@RequestBody @Valid OrderRequest orderRequest) {
        return ApiResponse.success(orderService.createOrder(orderRequest), "Order created successfully");
    }

    @GetMapping("/{orderCode}")
    @Operation(summary = "Lấy chi tiết đơn hàng theo mã (Order Code)", description = "Xem thông tin chi tiết của một đơn hàng cụ thể dựa trên mã code.")
    public ApiResponse<OrderResponse> getOrderByCode(@PathVariable String orderCode) {
        return ApiResponse.success(orderService.getOrderByCode(orderCode), "Order details retrieved successfully");
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy lịch sử đơn hàng của tôi", description = "Trả về danh sách đơn hàng có phân trang của người dùng hiện tại đang đăng nhập.")
    public ApiResponse<PageResponse<OrderResponse>> getMyOrders(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        PageResponse<OrderResponse> orderPage = orderService.getOrdersByUser(pageable);
        return ApiResponse.success(orderPage, "User's orders retrieved successfully");
    }

    @GetMapping("/check-delivery/{productId}")
    @Operation(summary = "Kiểm tra sản phẩm đã giao chưa", description = "Kiểm tra xem sản phẩm cụ thể đã được giao thành công tới người dùng hiện tại chưa (để cho phép đánh giá).")
    public ApiResponse<Boolean> checkProductDelivery(
            @PathVariable Long productId) {
        boolean isDelivered = orderService.isProductDeliveredToCurrentUser(productId);
        return ApiResponse.success(isDelivered, "Product delivery status checked successfully");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy toàn bộ đơn hàng hệ thống (Admin)", description = "Dành cho Admin: Xem và quản lý tất cả đơn hàng từ mọi người dùng trên hệ thống.")
    public ApiResponse<PageResponse<OrderResponse>> getAllOrders(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        PageResponse<OrderResponse> orderPage = orderService.getAllOrders(pageable);
        return ApiResponse.success(orderPage, "All orders retrieved successfully");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật đơn hàng", description = "Chỉnh sửa thông tin đơn hàng cụ thể theo ID.")
    public ApiResponse<OrderResponse> updateOrder(@PathVariable Long id, @RequestBody @Valid OrderRequest orderRequest) {
        return ApiResponse.success(orderService.updateOrder(id, orderRequest), "Order updated successfully");
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật trạng thái đơn hàng (Admin)", description = "Dành cho Admin: Cập nhật trạng thái của đơn hàng (CONFIRMED, SHIPPED, DELIVERED, CANCELLED...).")
    public ApiResponse<OrderResponse> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return ApiResponse.success(orderService.updateOrderStatus(id, status), "Order status updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hủy đơn hàng", description = "Người dùng tự hủy đơn hàng của mình. Hệ thống sẽ tự động hoàn lại kho và lượt dùng mã giảm giá.")
    public ApiResponse<String> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ApiResponse.success("Order canceled", "Order has been canceled successfully");
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa đơn hàng vĩnh viễn (Admin)", description = "Dành cho Admin: Xóa hoàn toàn bản ghi đơn hàng khỏi cơ sở dữ liệu.")
    public ApiResponse<String> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ApiResponse.success("Order deleted", "Order has been deleted successfully");
    }
}
