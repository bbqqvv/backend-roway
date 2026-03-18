package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.dto.request.CategoryRequest;
import org.bbqqvv.backendecommerce.dto.response.CategoryResponse;
import org.bbqqvv.backendecommerce.entity.Category;
import org.bbqqvv.backendecommerce.entity.SizeCategory;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.mapper.CategoryMapper;
import org.bbqqvv.backendecommerce.mapper.SizeMapper;
import org.bbqqvv.backendecommerce.repository.CategoryRepository;
import org.bbqqvv.backendecommerce.service.CategoryService;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;
    SizeMapper sizeMapper;
    CloudinaryService cloudinaryService;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper, SizeMapper sizeMapper, CloudinaryService cloudinaryService) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.sizeMapper = sizeMapper;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        try {
            if (categoryRepository.existsCategoriesByName(categoryRequest.getName())) {
                throw new AppException(UserErrorCode.USER_EXISTED);
            }

            String imageUrl = handleImageUpload(categoryRequest.getImage());

            Category category = categoryMapper.categoryRequestToCategory(categoryRequest);
            category.setImage(imageUrl);

            if (categoryRequest.getSizes() != null && !categoryRequest.getSizes().isEmpty()) {
                List<SizeCategory> sizeCategories = mapSizeCategories(categoryRequest, category);
                category.setSizeCategories(sizeCategories);
            }

            Category savedCategory = categoryRepository.save(category);
            return categoryMapper.categoryToCategoryResponse(savedCategory);
        } catch (IOException e) {
            log.error("Lỗi khi tải ảnh", e);
            throw new AppException(InfrastructureAddressErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));

            String imageUrl = handleImageUpload(categoryRequest.getImage());

            category.setSlug(categoryRequest.getSlug());
            category.setName(categoryRequest.getName());
            category.setImage(imageUrl);

            if (categoryRequest.getSizes() != null && !categoryRequest.getSizes().isEmpty()) {
                List<SizeCategory> sizeCategories = mapSizeCategories(categoryRequest, category);
                category.setSizeCategories(sizeCategories);
            }

            Category updatedCategory = categoryRepository.save(category);
            return categoryMapper.categoryToCategoryResponse(updatedCategory);
        } catch (IOException e) {
            log.error("Lỗi khi tải ảnh", e);
            throw new AppException(InfrastructureAddressErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::categoryToCategoryResponse)
                .orElseThrow(() -> new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::categoryToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
        return true;
    }

    private List<SizeCategory> mapSizeCategories(CategoryRequest categoryRequest, Category category) {
        return categoryRequest.getSizes().stream()
                .map(sizeMapper::toSize)
                .peek(size -> size.setCategory(category))
                .collect(Collectors.toList());
    }

    private String handleImageUpload(MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            return cloudinaryService.uploadImage(image);
        }
        return null;
    }
}

