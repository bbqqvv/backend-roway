package org.bbqqvv.backendecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.entity.ShippingRegionFee;
import org.bbqqvv.backendecommerce.entity.ShippingSetting;
import org.bbqqvv.backendecommerce.repository.ShippingRegionFeeRepository;
import org.bbqqvv.backendecommerce.repository.ShippingSettingRepository;
import org.bbqqvv.backendecommerce.service.ShippingService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingServiceImpl implements ShippingService {

    private final ShippingSettingRepository shippingSettingRepository;
    private final ShippingRegionFeeRepository shippingRegionFeeRepository;

    private static final String FREE_SHIPPING_THRESHOLD_KEY = "FREE_SHIPPING_THRESHOLD";
    private static final String DEFAULT_SHIPPING_FEE_KEY = "DEFAULT_SHIPPING_FEE";

    @Override
    @Cacheable(value = "shipping_settings", key = "'threshold'")
    public BigDecimal getFreeShippingThreshold() {
        return shippingSettingRepository.findByKey(FREE_SHIPPING_THRESHOLD_KEY)
                .map(s -> new BigDecimal(s.getValue()))
                .orElse(new BigDecimal("499000"));
    }

    @Override
    @Cacheable(value = "shipping_settings", key = "'default_fee'")
    public BigDecimal getDefaultShippingFee() {
        return shippingSettingRepository.findByKey(DEFAULT_SHIPPING_FEE_KEY)
                .map(s -> new BigDecimal(s.getValue()))
                .orElse(new BigDecimal("50000"));
    }

    @Override
    @Cacheable(value = "shipping_fees", key = "#regionName")
    public BigDecimal getShippingFeeByRegion(String regionName) {
        if (regionName == null) return getDefaultShippingFee();
        
        String normalizedRegion = regionName.trim().toLowerCase();
        return shippingRegionFeeRepository.findByRegionName(normalizedRegion)
                .map(ShippingRegionFee::getFee)
                .orElse(getDefaultShippingFee());
    }

    @Override
    @Cacheable(value = "shipping_fees", key = "'all'")
    public Map<String, BigDecimal> getAllRegionalFees() {
        return shippingRegionFeeRepository.findAll().stream()
                .collect(Collectors.toMap(
                        ShippingRegionFee::getRegionName,
                        ShippingRegionFee::getFee
                ));
    }
}
