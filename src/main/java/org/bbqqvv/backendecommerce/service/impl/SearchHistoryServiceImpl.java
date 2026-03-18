package org.bbqqvv.backendecommerce.service.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.request.SearchHistoryRequest;
import org.bbqqvv.backendecommerce.dto.response.SearchHistoryResponse;
import org.bbqqvv.backendecommerce.entity.SearchHistory;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.mapper.SearchHistoryMapper;
import org.bbqqvv.backendecommerce.repository.SearchHistoryRepository;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.bbqqvv.backendecommerce.service.SearchHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchHistoryServiceImpl implements SearchHistoryService {
    SearchHistoryMapper searchHistoryMapper;
    SearchHistoryRepository searchHistoryRepository;
    UserRepository userRepository;

    public SearchHistoryServiceImpl(SearchHistoryMapper searchHistoryMapper,
                                    SearchHistoryRepository searchHistoryRepository,
                                    UserRepository userRepository) {
        this.searchHistoryMapper = searchHistoryMapper;
        this.searchHistoryRepository = searchHistoryRepository;
        this.userRepository = userRepository;
    }

    // 🟢 Lấy user hiện tại từ SecurityUtils
    private User getAuthenticatedUser() {
        return SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findByUsername)
                .orElse(null); // ⬅️ Trả về null thay vì ném lỗi
    }


    // 🟢 Lưu lịch sử tìm kiếm của current user
    @Override
    public SearchHistoryResponse saveSearchQuery(SearchHistoryRequest request) {
        User user = getAuthenticatedUser();

        SearchHistory searchHistory = SearchHistory.builder()
                .searchQuery(request.getSearchQuery())
                .user(user)
                .build();

        searchHistoryRepository.save(searchHistory);
        return searchHistoryMapper.toResponse(searchHistory);
    }

    // 🟢 Lấy lịch sử tìm kiếm của current user (tối đa 5 từ khóa)
    @Override
    public List<SearchHistoryResponse> getUserSearchHistory() {
        User user = getAuthenticatedUser();
        if (user == null) return List.of(); // Không có user thì không có lịch sử riêng
        
        return searchHistoryRepository.findTop5ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(searchHistoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    // 🟢 Gợi ý tìm kiếm (autocomplete)
    @Override
    public List<SearchHistoryResponse> getSearchSuggestions(String query) {
        User user = getAuthenticatedUser();
        // Nếu đã log in, lấy trong lịch sử cá nhân trước
        if (user != null) {
            List<SearchHistory> suggestions = searchHistoryRepository.findTop5ByUserAndSearchQueryContainingIgnoreCaseOrderByCreatedAtDesc(user, query);
            if (!suggestions.isEmpty()) {
                return suggestions.stream()
                        .map(searchHistoryMapper::toResponse)
                        .collect(Collectors.toList());
            }
        }
        
        // Nếu không có lịch sử cá nhân, lấy 5 từ khóa chung gần đây nhất (anonymized)
        return searchHistoryRepository.findTop5ByUserAndSearchQueryContainingIgnoreCaseOrderByCreatedAtDesc(null, query)
                .stream()
                .map(searchHistoryMapper::toResponse)
                .collect(Collectors.toList());
    }
}
