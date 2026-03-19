package org.bbqqvv.backendecommerce.service.payment;

import org.bbqqvv.backendecommerce.entity.Order;
import org.bbqqvv.backendecommerce.entity.PaymentMethod;

import java.util.Map;

public interface PaymentStrategy {
    PaymentMethod getMethod();
    
    // Tạo link thanh toán
    String createPaymentUrl(Order order);
    
    // Xác thực kết quả từ NCC Dịch vụ
    boolean verifyCallback(Map<String, String> params);
}
