package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.request.SearchHistoryRequest;
import org.bbqqvv.backendecommerce.dto.response.SearchHistoryResponse;
import org.bbqqvv.backendecommerce.entity.SearchHistory;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.mapper.SearchHistoryMapper;
import org.bbqqvv.backendecommerce.repository.SearchHistoryRepository;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchHistoryServiceTest {

    @Mock private SearchHistoryRepository searchHistoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private SearchHistoryMapper searchHistoryMapper;

    @InjectMocks
    private SearchHistoryServiceImpl searchHistoryService;

    private User user;
    private SearchHistory searchHistory;
    private SearchHistoryRequest searchHistoryRequest;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        searchHistory = SearchHistory.builder()
                .id(1L)
                .searchQuery("Roway T-Shirt")
                .user(user)
                .build();
        searchHistoryRequest = new SearchHistoryRequest();
        searchHistoryRequest.setSearchQuery("Roway T-Shirt");
    }

    @Test
    @DisplayName("Lưu lịch sử tìm kiếm thành công - Authenticated User")
    void saveSearchQuery_shouldSaveWithUser_whenAuthenticated() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(searchHistoryMapper.toResponse(any())).thenReturn(new SearchHistoryResponse());

            searchHistoryService.saveSearchQuery(searchHistoryRequest);

            verify(searchHistoryRepository).save(argThat(sh -> sh.getUser() != null && sh.getUser().getId().equals(1L)));
        }
    }

    @Test
    @DisplayName("Lưu lịch sử tìm kiếm thành công - Anonymous User")
    void saveSearchQuery_shouldSaveWithoutUser_whenAnonymous() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.empty());
            when(searchHistoryMapper.toResponse(any())).thenReturn(new SearchHistoryResponse());

            searchHistoryService.saveSearchQuery(searchHistoryRequest);

            verify(searchHistoryRepository).save(argThat(sh -> sh.getUser() == null));
        }
    }

    @Test
    @DisplayName("Lấy lịch sử tìm kiếm của User")
    void getUserSearchHistory_shouldReturnList() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(searchHistoryRepository.findTop5ByUserOrderByCreatedAtDesc(user))
                    .thenReturn(List.of(searchHistory));

            List<SearchHistoryResponse> result = searchHistoryService.getUserSearchHistory();

            assertThat(result).hasSize(1);
            verify(searchHistoryRepository).findTop5ByUserOrderByCreatedAtDesc(user);
        }
    }

    @Test
    @DisplayName("Gợi ý tìm kiếm cá nhân hóa")
    void getSearchSuggestions_shouldReturnPersonalized_whenUserLoggedIn() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(searchHistoryRepository.findTop5ByUserAndSearchQueryContainingIgnoreCaseOrderByCreatedAtDesc(user, "Roway"))
                    .thenReturn(List.of(searchHistory));

            List<SearchHistoryResponse> result = searchHistoryService.getSearchSuggestions("Roway");

            assertThat(result).hasSize(1);
            verify(searchHistoryRepository).findTop5ByUserAndSearchQueryContainingIgnoreCaseOrderByCreatedAtDesc(eq(user), eq("Roway"));
        }
    }

    @Test
    @DisplayName("Gợi ý tìm kiếm toàn cục khi chưa đăng nhập")
    void getSearchSuggestions_shouldReturnGlobal_whenUserNotLoggedIn() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.empty());
            when(searchHistoryRepository.findTop5ByUserAndSearchQueryContainingIgnoreCaseOrderByCreatedAtDesc(null, "Roway"))
                    .thenReturn(List.of(searchHistory));

            searchHistoryService.getSearchSuggestions("Roway");

            verify(searchHistoryRepository).findTop5ByUserAndSearchQueryContainingIgnoreCaseOrderByCreatedAtDesc(isNull(), eq("Roway"));
        }
    }
}
