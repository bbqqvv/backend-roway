package org.bbqqvv.backendecommerce.service.payment;

import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.entity.Order;
import org.bbqqvv.backendecommerce.entity.PaymentMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final List<PaymentStrategy> strategies;
    
    private Map<PaymentMethod, PaymentStrategy> strategyMap;
    
    // Lazy init map
    private Map<PaymentMethod, PaymentStrategy> getStrategyMap() {
        if (strategyMap == null) {
            strategyMap = strategies.stream()
                    .collect(Collectors.toMap(PaymentStrategy::getMethod, s -> s));
        }
        return strategyMap;
    }
    
    public String createPaymentUrl(Order order) {
        PaymentStrategy strategy = getStrategyMap().get(order.getPaymentMethod());
        if (strategy == null) {
            return null; // For COD or unsupported methods
        }
        return strategy.createPaymentUrl(order);
    }
    
    public boolean verifyCallback(PaymentMethod method, Map<String, String> params) {
        PaymentStrategy strategy = getStrategyMap().get(method);
        if (strategy == null) return false;
        return strategy.verifyCallback(params);
    }
}
