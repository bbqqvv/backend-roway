package org.bbqqvv.backendecommerce.service;

import jakarta.validation.Valid;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.DiscountPreviewRequest;
import org.bbqqvv.backendecommerce.dto.request.DiscountRequest;
import org.bbqqvv.backendecommerce.dto.response.DiscountPreviewResponse;
import org.bbqqvv.backendecommerce.dto.response.DiscountResponse;
import org.springframework.data.domain.Pageable;
import org.bbqqvv.backendecommerce.entity.Discount;
import java.math.BigDecimal;
import java.util.List;

public interface DiscountService {
    DiscountResponse createDiscount(DiscountRequest request);
    DiscountResponse getDiscountById(Long id);
    PageResponse<DiscountResponse> getAllDiscounts(Pageable pageable);
    DiscountResponse updateDiscount(Long id, DiscountRequest request);
    void deleteDiscount(Long id);
    void clearUsersAndProducts(Long id);

    void removeProductsFromDiscount(Long id, List<Long> productIds);

    void removeUsersFromDiscount(Long id, List<Long> userIds);

    PageResponse<DiscountResponse> getCurrentUserDiscount(Pageable pageable);

    DiscountPreviewResponse previewDiscount(DiscountPreviewRequest discountPreviewRequest);
    void saveDiscount(@Valid String discountCode);
    PageResponse<Long> getApplicableProductIds(Long discountId, Pageable pageable);
    PageResponse<Long> getApplicableUserIds(Long discountId, Pageable pageable);

    Discount getDiscountByCode(String code);
    BigDecimal calculateDiscountAmount(Discount discount, List<Long> productIds, List<BigDecimal> subtotals, BigDecimal totalAmount);
}
