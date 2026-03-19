package org.bbqqvv.backendecommerce.repository;

import org.bbqqvv.backendecommerce.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByOrderOrderCode(String orderCode);
}
