package org.bbqqvv.backendecommerce.service.payment.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.entity.Order;
import org.bbqqvv.backendecommerce.entity.PaymentMethod;
import org.bbqqvv.backendecommerce.service.payment.PaymentStrategy;
import org.bbqqvv.backendecommerce.util.VNPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class VNPayPaymentStrategy implements PaymentStrategy {

    @Value("${payment.vnPay.url}")
    private String vnp_PayUrl;

    @Value("${payment.vnPay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${payment.vnPay.secretKey}")
    private String vnp_HashSecret;

    @Value("${payment.vnPay.returnUrl}")
    private String vnp_ReturnUrl;

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public String createPaymentUrl(Order order) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = order.getOrderCode();
        String vnp_IpAddr = getClientIp();

        String vnp_TmnCodeLocal = vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCodeLocal);
        vnp_Params.put("vnp_Amount", String.valueOf(order.getTotalAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue()));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnp_PayUrl + "?" + queryUrl;
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String hashContent = VNPayUtil.hashAllFields(params, vnp_HashSecret);
        return hashContent != null && hashContent.equalsIgnoreCase(secureHash);
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return VNPayUtil.getIpAddress(attrs.getRequest());
        }
        return "127.0.0.1";
    }
}
