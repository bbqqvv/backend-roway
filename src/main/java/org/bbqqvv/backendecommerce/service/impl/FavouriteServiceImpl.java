package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.response.FavouriteResponse;
import org.bbqqvv.backendecommerce.entity.Favourite;
import org.bbqqvv.backendecommerce.entity.Product;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.mapper.FavouriteMapper;
import org.bbqqvv.backendecommerce.repository.FavouriteRepository;
import org.bbqqvv.backendecommerce.repository.ProductRepository;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.bbqqvv.backendecommerce.service.FavouriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.bbqqvv.backendecommerce.util.PagingUtil.toPageResponse;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FavouriteServiceImpl implements FavouriteService {

    FavouriteRepository favouriteRepository;
    ProductRepository productRepository;
    UserRepository userRepository;
    FavouriteMapper favouriteMapper;

    public FavouriteServiceImpl(FavouriteRepository favouriteRepository, ProductRepository productRepository,
                                UserRepository userRepository, FavouriteMapper favouriteMapper) {
        this.favouriteRepository = favouriteRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.favouriteMapper = favouriteMapper;
    }

    @Override
    @Transactional
    public FavouriteResponse addFavourite(Long productId) {
        User user = getAuthenticatedUser();

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if (favouriteRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new AppException(SocialMarketingErrorCode.FAVOURITE_ALREADY_EXISTS);
        }

        Favourite favourite = new Favourite();
        favourite.setUser(user);
        favourite.setProduct(product);

        Favourite savedFavourite = favouriteRepository.save(favourite);
        FavouriteResponse response = favouriteMapper.toFavouriteResponse(savedFavourite);

        // ✅ Tính toán stockStatus trong service
        response.setStockStatus(getStockStatus(product));

        return response;
    }

    @Override
    @Transactional
    public FavouriteResponse removeFavourite(Long favouriteId) {
        User user = getAuthenticatedUser();

        Favourite existingFavourite = favouriteRepository.findById(favouriteId)
                .orElseThrow(() -> new AppException(SocialMarketingErrorCode.FAVOURITE_NOT_FOUND));

        favouriteRepository.delete(existingFavourite);

        FavouriteResponse response = FavouriteResponse.builder()
                .id(existingFavourite.getId())
                .userId(existingFavourite.getUser().getId())
                .nameProduct(existingFavourite.getProduct().getName())
                .imageUrl(existingFavourite.getProduct().getMainImage().getImageUrl())
                .productUrl(existingFavourite.getProduct().getSlug()) // ✅ Lấy productUrl
                .build();

        // ✅ Tính toán stockStatus trong service
        response.setStockStatus(getStockStatus(existingFavourite.getProduct()));

        return response;
    }

    @Override
    public PageResponse<FavouriteResponse> getUserFavourites(Pageable pageable) {
        User user = getAuthenticatedUser();

        Page<Favourite> favouritesPage = favouriteRepository.findByUserId(user.getId(), pageable);

        return toPageResponse(favouritesPage, favourite -> {
            FavouriteResponse response = favouriteMapper.toFavouriteResponse(favourite);
            response.setStockStatus(getStockStatus(favourite.getProduct()));
            return response;
        });
    }


    private User getAuthenticatedUser() {
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(CommonErrorCode.UNAUTHENTICATED));

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
    }

    /**
     * ✅ Phương thức tính toán StockStatus (Còn hàng / Hết hàng)
     */
    private String getStockStatus(Product product) {
        return product.getVariants().stream()
                .flatMap(v -> v.getProductVariantSizes().stream())
                .anyMatch(size -> size.getStock() > 0) ? "Còn hàng" : "Hết hàng";
    }
}

