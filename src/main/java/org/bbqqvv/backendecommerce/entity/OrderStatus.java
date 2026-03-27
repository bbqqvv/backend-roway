package org.bbqqvv.backendecommerce.entity;

public enum OrderStatus {
    PENDING,      // Chờ xác nhận
    CONFIRMED,    // Đã xác nhận, đang xử lý
    SHIPPED,      // Đang giao hàng
    DELIVERED,    // Đã giao hàng thành công
    CANCELED,     // Đã hủy đơn hàng
    REFUND_REQUESTED, // Yêu cầu hoàn tiền/đổi trả
    REFUNDED,     // Đã hoàn tiền
    RETURNED      // Đã trả hàng
}
