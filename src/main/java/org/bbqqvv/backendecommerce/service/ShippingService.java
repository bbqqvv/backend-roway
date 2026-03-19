package org.bbqqvv.backendecommerce.service;

import java.math.BigDecimal;
import java.util.Map;

public interface ShippingService {
    BigDecimal getFreeShippingThreshold();
    BigDecimal getDefaultShippingFee();
    BigDecimal getShippingFeeByRegion(String regionName);
    Map<String, BigDecimal> getAllRegionalFees();
}
