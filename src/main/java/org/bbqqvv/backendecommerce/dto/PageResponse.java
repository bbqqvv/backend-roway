package org.bbqqvv.backendecommerce.dto;

import java.util.Collections;
import java.util.List;

public record PageResponse<T>(
    int currentPage,
    int totalPages,
    int pageSize,
    long totalElements,
    List<T> items
) {
    public PageResponse {
        if (items == null) {
            items = Collections.emptyList();
        }
    }
}
