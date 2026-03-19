package org.bbqqvv.backendecommerce.repository;

import org.bbqqvv.backendecommerce.entity.ShippingSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShippingSettingRepository extends JpaRepository<ShippingSetting, Long> {
    Optional<ShippingSetting> findByKey(String key);
}
