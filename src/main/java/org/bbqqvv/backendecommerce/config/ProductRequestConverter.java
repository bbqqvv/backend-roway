package org.bbqqvv.backendecommerce.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.request.ProductVariantRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductRequestConverter implements Converter<String, List<ProductVariantRequest>> {

    private final ObjectMapper objectMapper;

    @Override
    public List<ProductVariantRequest> convert(String source) {
        try {
            return objectMapper.readValue(source, new TypeReference<List<ProductVariantRequest>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JSON format for variants: " + e.getMessage());
        }
    }
}
