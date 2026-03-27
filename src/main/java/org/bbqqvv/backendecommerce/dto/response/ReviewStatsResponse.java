package org.bbqqvv.backendecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsResponse {
    private double averageRating;
    private long totalReviews;
    private Map<Integer, Long> distribution; // key: 1-5 sao, value: count
}
