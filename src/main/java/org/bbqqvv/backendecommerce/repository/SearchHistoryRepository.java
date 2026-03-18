package org.bbqqvv.backendecommerce.repository;

import org.bbqqvv.backendecommerce.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    // Lấy 5 từ khóa tìm kiếm gần nhất của một user nhất định
    List<SearchHistory> findTop5ByUserOrderByCreatedAtDesc(org.bbqqvv.backendecommerce.entity.User user);

    // Gợi ý tìm kiếm theo từ khóa (giống LIKE '%query%')
    List<SearchHistory> findTop5ByUserAndSearchQueryContainingIgnoreCaseOrderByCreatedAtDesc(org.bbqqvv.backendecommerce.entity.User user, String query);
}
