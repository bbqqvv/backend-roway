package org.bbqqvv.backendecommerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.repository.OrderRepository;
import org.bbqqvv.backendecommerce.repository.PaymentRepository;
import org.bbqqvv.backendecommerce.service.payment.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "Xử lý thanh toán và Callback từ cổng thanh toán")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("/vn-pay-callback")
    @Operation(summary = "Xử lý Callback từ VNPay (IPN)", description = "Xác thực chữ ký từ VNPay, cập nhật trạng thái đơn hàng sang CONFIRMED nếu thanh toán thành công và lưu thông tin vào bảng Payment.")
    public ApiResponse<String> vnPayCallback(@RequestParam Map<String, String> queryParams) {
        log.info("Received VNPay callback: {}", queryParams);
        
        boolean isValid = paymentService.verifyCallback(PaymentMethod.VNPAY, queryParams);
        if (!isValid) {
            return ApiResponse.error("Invalid signature");
        }

        String orderCode = queryParams.get("vnp_TxnRef");
        String responseCode = queryParams.get("vnp_ResponseCode");
        String transactionId = queryParams.get("vnp_TransactionNo");
        String amountStr = queryParams.get("vnp_Amount");

        Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
        if (order == null) {
            return ApiResponse.error("Order not found");
        }

        // 00 = Success in VNPay
        if ("00".equals(responseCode)) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            // Log payment
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)))
                    .paymentMethod(PaymentMethod.VNPAY)
                    .status(PaymentStatus.SUCCESS)
                    .transactionId(transactionId)
                    .paymentDate(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            log.info("Order {} marked as CONFIRMED after successful VNPay payment", orderCode);
            return ApiResponse.success("Success", "Payment successful");
        } else {
            log.warn("Payment failed for Order {} with ResponseCode {}", orderCode, responseCode);
            return ApiResponse.error("Payment failed with code: " + responseCode);
        }
    }
}
