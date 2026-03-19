package org.bbqqvv.backendecommerce.repository;

import org.bbqqvv.backendecommerce.entity.ShippingRegionFee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShippingRegionFeeRepository extends JpaRepository<ShippingRegionFee, Long> {
    Optional<ShippingRegionFee> findByRegionName(String regionName);
}
