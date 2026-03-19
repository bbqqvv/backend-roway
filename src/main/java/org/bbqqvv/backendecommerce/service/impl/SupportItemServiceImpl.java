package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.dto.request.ImageMetadata;
import org.bbqqvv.backendecommerce.dto.request.SupportItemRequest;
import org.bbqqvv.backendecommerce.dto.response.SupportItemResponse;
import org.bbqqvv.backendecommerce.entity.SupportItem;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.mapper.SupportItemMapper;
import org.bbqqvv.backendecommerce.repository.SupportItemRepository;
import org.bbqqvv.backendecommerce.service.SupportItemsService;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class SupportItemServiceImpl implements SupportItemsService {

    SupportItemRepository supportItemRepository;
    SupportItemMapper supportItemMapper;
    CloudinaryService cloudinaryService;

    public SupportItemServiceImpl(SupportItemRepository supportItemRepository,
                                  SupportItemMapper supportItemMapper,
                                  CloudinaryService cloudinaryService) {
        this.supportItemRepository = supportItemRepository;
        this.supportItemMapper = supportItemMapper;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public List<SupportItemResponse> getAllSupportItems() {
        log.info("Fetching all support items...");
        return supportItemRepository.findAll().stream()
                .map(supportItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SupportItemResponse getSupportItemById(Long id) {
        log.info("Fetching support item with id: {}", id);
        return supportItemRepository.findById(id)
                .map(supportItemMapper::toResponse)
                .orElseThrow(() -> new AppException(CommonErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public SupportItemResponse createSupportItem(SupportItemRequest request) {
        log.info("Creating support item: {}", request.getTitle());

        String imageUrl = null;
        if (request.getImg() != null && !request.getImg().isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(request.getImg()).getUrl();
        }

        SupportItem supportItem = SupportItem.builder()
                .title(request.getTitle())
                .img(imageUrl)
                .hours(request.getHours())
                .contact(request.getContact())
                .link(request.getLink())
                .bgColor(request.getBgColor())
                .build();

        return supportItemMapper.toResponse(supportItemRepository.save(supportItem));
    }

    @Override
    public SupportItemResponse updateSupportItem(Long id, SupportItemRequest request) {
        log.info("Updating support item with id: {}", id);

        SupportItem existing = supportItemRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.RESOURCE_NOT_FOUND));

        String imageUrl = existing.getImg(); // mặc định giữ nguyên ảnh cũ
        if (request.getImg() != null && !request.getImg().isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(request.getImg()).getUrl();
        }

        existing.setTitle(request.getTitle());
        existing.setImg(imageUrl);
        existing.setHours(request.getHours());
        existing.setContact(request.getContact());
        existing.setLink(request.getLink());
        existing.setBgColor(request.getBgColor());

        return supportItemMapper.toResponse(supportItemRepository.save(existing));
    }

    @Override
    public void deleteSupportItem(Long id) {
        log.info("Deleting support item with id: {}", id);
        SupportItem supportItem = supportItemRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.RESOURCE_NOT_FOUND));
        supportItemRepository.delete(supportItem);
    }
}

