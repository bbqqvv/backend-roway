package org.bbqqvv.backendecommerce.service;

import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecentlyViewedProductService {

    void markProductAsViewed(Long productId);

    PageResponse<ProductResponse> getRecentlyViewedProducts(Pageable pageable);

    void syncViewedProducts(List<Long> productIds);
    void clearRecentlyViewedProducts();
}
