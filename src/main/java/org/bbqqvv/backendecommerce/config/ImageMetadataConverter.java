package org.bbqqvv.backendecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.request.ImageMetadata;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ImageMetadataConverter implements Converter<String, ImageMetadata> {

    private final ObjectMapper objectMapper;

    @Override
    public ImageMetadata convert(String source) {
        try {
            return objectMapper.readValue(source, ImageMetadata.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JSON format for ImageMetadata: " + e.getMessage());
        }
    }
}
