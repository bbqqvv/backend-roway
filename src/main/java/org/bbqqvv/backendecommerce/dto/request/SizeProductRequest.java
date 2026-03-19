package org.bbqqvv.backendecommerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor  
@AllArgsConstructor
public class SizeProductRequest {
    private String sizeName;
    @Min(value = 0, message = "Stock must be greater than or equal to 0")
    private int stock;
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    public void setQuantity(int quantity) {
        this.stock = quantity;
    }
}
