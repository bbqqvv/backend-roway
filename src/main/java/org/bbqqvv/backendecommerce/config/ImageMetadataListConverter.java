package org.bbqqvv.backendecommerce.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.request.ImageMetadata;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ImageMetadataListConverter implements Converter<String, List<ImageMetadata>> {

    private final ObjectMapper objectMapper;

    @Override
    public List<ImageMetadata> convert(String source) {
        try {
            return objectMapper.readValue(source, new TypeReference<List<ImageMetadata>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JSON format for List<ImageMetadata>: " + e.getMessage());
        }
    }
}
