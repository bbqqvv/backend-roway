package org.bbqqvv.backendecommerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    private String reason;
    private String type; // "REFUND" or "EXCHANGE"
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
}
