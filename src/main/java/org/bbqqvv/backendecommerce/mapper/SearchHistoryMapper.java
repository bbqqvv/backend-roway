package org.bbqqvv.backendecommerce.mapper;

import org.bbqqvv.backendecommerce.dto.response.SearchHistoryResponse;
import org.bbqqvv.backendecommerce.entity.SearchHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @org.mapstruct.Builder(disableBuilder = true))
public interface SearchHistoryMapper {
    @Mapping(source = "searchQuery", target = "searchQuery")
    SearchHistoryResponse toResponse(SearchHistory searchHistory);
}
