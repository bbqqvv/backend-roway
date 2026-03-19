package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.response.FavouriteResponse;
import org.bbqqvv.backendecommerce.entity.Favourite;
import org.bbqqvv.backendecommerce.entity.Product;
import org.bbqqvv.backendecommerce.entity.ProductImage;
import org.bbqqvv.backendecommerce.entity.ProductMainImage;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.SocialMarketingErrorCode;
import org.bbqqvv.backendecommerce.mapper.FavouriteMapper;
import org.bbqqvv.backendecommerce.repository.FavouriteRepository;
import org.bbqqvv.backendecommerce.repository.ProductRepository;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavouriteServiceTest {

    @Mock private FavouriteRepository favouriteRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private FavouriteMapper favouriteMapper;

    @InjectMocks
    private FavouriteServiceImpl favouriteService;

    private User user;
    private Product product;
    private Favourite favourite;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        product = Product.builder().id(101L).name("Roway Jeans").variants(new ArrayList<>()).build();
        favourite = new Favourite();
        favourite.setId(1L);
        favourite.setUser(user);
        favourite.setProduct(product);
    }

    @Test
    @DisplayName("Thêm sản phẩm yêu thích thành công")
    void addFavourite_shouldSave_whenNotAlreadyExists() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(productRepository.findById(101L)).thenReturn(Optional.of(product));
            when(favouriteRepository.existsByProductIdAndUserId(101L, 1L)).thenReturn(false);
            when(favouriteRepository.save(any())).thenReturn(favourite);
            when(favouriteMapper.toFavouriteResponse(any())).thenReturn(new FavouriteResponse());

            FavouriteResponse response = favouriteService.addFavourite(101L);

            assertThat(response).isNotNull();
            verify(favouriteRepository).save(any(Favourite.class));
        }
    }

    @Test
    @DisplayName("Thêm sản phẩm yêu thích thất bại - Đã tồn tại")
    void addFavourite_shouldThrowException_whenAlreadyExists() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(productRepository.findById(101L)).thenReturn(Optional.of(product));
            when(favouriteRepository.existsByProductIdAndUserId(101L, 1L)).thenReturn(true);

            assertThatThrownBy(() -> favouriteService.addFavourite(101L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", SocialMarketingErrorCode.FAVOURITE_ALREADY_EXISTS);
        }
    }

    @Test
    @DisplayName("Xóa sản phẩm yêu thích thành công")
    void removeFavourite_shouldDelete_whenFound() {
        ProductMainImage mainImage = ProductMainImage.builder()
                .imageUrl("test.jpg")
                .build();
        product.setMainImage(mainImage);
        product.setSlug("jeans");

        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(favouriteRepository.findById(1L)).thenReturn(Optional.of(favourite));

            FavouriteResponse response = favouriteService.removeFavourite(1L);

            assertThat(response).isNotNull();
            verify(favouriteRepository).delete(favourite);
        }
    }

    @Test
    @DisplayName("Lấy danh sách yêu thích của User")
    void getUserFavourites_shouldReturnPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            
            Page<Favourite> page = new PageImpl<>(List.of(favourite));
            when(favouriteRepository.findByUserId(1L, pageable)).thenReturn(page);
            when(favouriteMapper.toFavouriteResponse(any())).thenReturn(new FavouriteResponse());

            PageResponse<FavouriteResponse> response = favouriteService.getUserFavourites(pageable);

            assertThat(response.items()).hasSize(1);
        }
    }
}
