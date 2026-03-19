package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.DiscountPreviewRequest;
import org.bbqqvv.backendecommerce.dto.request.DiscountRequest;
import org.bbqqvv.backendecommerce.dto.response.DiscountPreviewResponse;
import org.bbqqvv.backendecommerce.dto.response.DiscountResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.mapper.DiscountMapper;
import org.bbqqvv.backendecommerce.mapper.DiscountPreviewMapper;
import org.bbqqvv.backendecommerce.repository.*;
import org.bbqqvv.backendecommerce.service.CartService;
import org.bbqqvv.backendecommerce.service.DiscountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.bbqqvv.backendecommerce.util.PagingUtil.toPageResponse;

@Service
@Transactional
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final DiscountMapper discountMapper;
    private final DiscountPreviewMapper discountPreviewMapper;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final DiscountProductRepository discountProductRepository;
    private final DiscountUserRepository discountUserRepository;
    private final CartRepository cartRepository;

    public DiscountServiceImpl(DiscountRepository discountRepository,
                               DiscountMapper discountMapper, DiscountPreviewMapper discountPreviewMapper,
                               ProductRepository productRepository,
                               UserRepository userRepository,
                               DiscountProductRepository discountProductRepository,
                               DiscountUserRepository discountUserRepository, CartRepository cartRepository) {
        this.discountRepository = discountRepository;
        this.discountMapper = discountMapper;
        this.discountPreviewMapper = discountPreviewMapper;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.discountProductRepository = discountProductRepository;
        this.discountUserRepository = discountUserRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public DiscountResponse createDiscount(DiscountRequest request) {
        if (discountRepository.existsByCode(request.getCode())) {
            throw new AppException(SocialMarketingErrorCode.DUPLICATE_DISCOUNT_CODE);
        }

        // Dùng Builder để tạo Discount
        Discount discount = Discount.builder()
                .code(request.getCode())
                .discountAmount(request.getDiscountAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .discountType(request.getDiscountType())
                .minOrderValue(request.getMinOrderValue())
                .usageLimit(request.getUsageLimit())
                .startDate(request.getStartDate())
                .expiryDate(request.getExpiryDate())
                .timesUsed(0)
                .active(request.isActive())
                .build();

        List<DiscountProduct> discountProducts = getDiscountProducts(discount, request.getApplicableProducts());
        List<DiscountUser> discountUsers = getDiscountUsers(discount, request.getApplicableUsers());

        discount.setApplicableProducts(discountProducts);
        discount.setApplicableUsers(discountUsers);

        discount = discountRepository.saveAndFlush(discount);


        return discountMapper.toDiscountResponse(discountRepository.save(discount));
    }

    /**
     * Lấy danh sách sản phẩm từ database
     */
    private List<DiscountProduct> getDiscountProducts(Discount discount, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return List.of();

        // Lấy danh sách sản phẩm từ DB
        List<Product> products = productRepository.findAllById(productIds);

        // Kiểm tra xem có sản phẩm nào không tồn tại
        Set<Long> foundProductIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        List<Long> missingProductIds = productIds.stream()
                .filter(id -> !foundProductIds.contains(id))
                .toList();

        if (!missingProductIds.isEmpty()) {
            throw new AppException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        return products.stream()
                .map(product -> new DiscountProduct(discount, product))
                .collect(Collectors.toList());
    }


    /**
     * Lấy danh sách người dùng từ database
     */
    private List<DiscountUser> getDiscountUsers(Discount discount, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();

        // Lấy danh sách người dùng từ DB
        List<User> users = userRepository.findAllById(userIds);

        // Kiểm tra xem có user nào không tồn tại
        Set<Long> foundUserIds = users.stream().map(User::getId).collect(Collectors.toSet());
        List<Long> missingUserIds = userIds.stream()
                .filter(id -> !foundUserIds.contains(id))
                .toList();

        if (!missingUserIds.isEmpty()) {
            throw new AppException(UserErrorCode.USER_NOT_FOUND);
        }

        return users.stream()
                .map(user -> new DiscountUser(discount, user))
                .collect(Collectors.toList());
    }




    @Override
    @Transactional(readOnly = true)
    public DiscountResponse getDiscountById(Long id) {
        return discountMapper.toDiscountResponse(findDiscountById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DiscountResponse> getAllDiscounts(Pageable pageable) {
        Page<Discount> discountPage = discountRepository.findAll(pageable);
        return toPageResponse(discountPage, discountMapper::toDiscountResponse);
    }


    @Override
    public DiscountResponse updateDiscount(Long id, DiscountRequest request) {
        Discount discount = findDiscountById(id);

        if (discount.isExpired()) {
            throw new AppException(SocialMarketingErrorCode.DISCOUNT_ALREADY_EXPIRED);
        }

        // Cập nhật thông tin discount
        discount = discount.toBuilder()
                .code(request.getCode())
                .discountAmount(request.getDiscountAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .discountType(request.getDiscountType())
                .minOrderValue(request.getMinOrderValue())
                .usageLimit(request.getUsageLimit())
                .startDate(request.getStartDate())
                .expiryDate(request.getExpiryDate())
                .active(request.isActive())
                .build();

        discount = discountRepository.save(discount);

        updateDiscountProducts(discount, request.getApplicableProducts());
        updateDiscountUsers(discount, request.getApplicableUsers());

        return discountMapper.toDiscountResponse(discount);
    }

    @Override
    public void deleteDiscount(Long id) {
        Discount discount = findDiscountById(id);

        if (discount.isActive()) {
            throw new AppException(SocialMarketingErrorCode.CANNOT_DELETE_ACTIVE_DISCOUNT);
        }

        // ✅ Xóa quan hệ trong bảng trung gian trước khi xóa discount
        discountProductRepository.deleteByDiscountId(id);
        discountUserRepository.deleteByDiscountId(id);

        discountRepository.delete(discount);
    }

    @Override
    public void clearUsersAndProducts(Long id) {
        Discount discount = findDiscountById(id);

        // Xóa tất cả sản phẩm khỏi mã giảm giá
        discountProductRepository.deleteByDiscountId(id);

        // Xóa tất cả người dùng khỏi mã giảm giá
        discountUserRepository.deleteByDiscountId(id);
    }


    @Override
    public void removeProductsFromDiscount(Long id, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return;

        // Chuyển danh sách thành Set để tìm kiếm nhanh hơn
        Set<Long> productIdSet = new HashSet<>(productIds);

        // Lấy danh sách sản phẩm đang được áp dụng cho discount
        Set<Long> existingProductIds = discountProductRepository.findByDiscountId(id)
                .stream()
                .map(dp -> dp.getProduct().getId())
                .collect(Collectors.toSet());

        // Xác định các sản phẩm hợp lệ cần xóa
        productIdSet.retainAll(existingProductIds); // Chỉ giữ lại các ID tồn tại

        if (productIdSet.isEmpty()) {
            throw new AppException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        // Xóa sản phẩm khỏi discount
        discountProductRepository.deleteByDiscountIdAndProductIds(id, productIdSet);
    }

    @Override
    public void removeUsersFromDiscount(Long id, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;

        // Chuyển danh sách thành Set để tìm kiếm nhanh hơn
        Set<Long> userIdSet = new HashSet<>(userIds);

        // Lấy danh sách người dùng đang được áp dụng cho discount
        Set<Long> existingUserIds = discountUserRepository.findByDiscountId(id)
                .stream()
                .map(du -> du.getUser().getId())
                .collect(Collectors.toSet());

        // Xác định các user hợp lệ cần xóa
        userIdSet.retainAll(existingUserIds);

        if (userIdSet.isEmpty()) {
            throw new AppException(UserErrorCode.USER_NOT_FOUND);
        }

        // Xóa người dùng khỏi discount
        discountUserRepository.deleteByDiscountIdAndUserIds(id, userIdSet);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DiscountResponse> getCurrentUserDiscount(Pageable pageable) {
        User currentUser = getAuthenticatedUser();
        Page<Discount> discountPage = discountUserRepository.findDiscountsByUserId(currentUser.getId(), pageable);
        return toPageResponse(discountPage, discountMapper::toDiscountResponse);
    }


    private User getAuthenticatedUser() {
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(CommonErrorCode.UNAUTHENTICATED));

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
    }


    @Override
    public DiscountPreviewResponse previewDiscount(DiscountPreviewRequest discountPreviewRequest) {
        if (discountPreviewRequest == null || discountPreviewRequest.getDiscountCode() == null) {
            throw new AppException(CommonErrorCode.INVALID_REQUEST);
        }
        Discount discount = discountRepository.findByCode(discountPreviewRequest.getDiscountCode())
                .orElseThrow(() -> new AppException(SocialMarketingErrorCode.DISCOUNT_NOT_FOUND));

        User currentUser = getAuthenticatedUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AppException(CartOrderErrorCode.CART_NOT_FOUND));

        BigDecimal originalTotalAmount = cart.getTotalPrice();
        if (originalTotalAmount == null) originalTotalAmount = BigDecimal.ZERO;

        boolean valid = validateDiscount(discount, originalTotalAmount, currentUser);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (valid) {
            // Chuẩn bị dữ liệu cho hàm tính toán chung
            List<Long> productIds = cart.getCartItems().stream().map(item -> item.getProduct().getId()).toList();
            List<BigDecimal> subtotals = cart.getCartItems().stream().map(CartItem::getSubtotal).toList();
            
            discountAmount = calculateDiscountAmount(discount, productIds, subtotals, originalTotalAmount);
        }

        BigDecimal finalAmount = originalTotalAmount.subtract(discountAmount).max(BigDecimal.ZERO);

        return DiscountPreviewResponse.builder()
                .discountCode(discount.getCode())
                .discountType(discount.getDiscountType())
                .originalTotalAmount(originalTotalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .valid(valid)
                .message(valid ? "Discount applied successfully" : "Discount not applicable")
                .build();
    }

    @Override
    public Discount getDiscountByCode(String code) {
        return discountRepository.findByCode(code).orElse(null);
    }

    @Override
    public BigDecimal calculateDiscountAmount(Discount discount, List<Long> productIds, List<BigDecimal> subtotals, BigDecimal totalAmount) {
        BigDecimal applicableTotal = BigDecimal.ZERO;
        
        Set<Long> applicableProductIds = Collections.emptySet();
        boolean hasSpecificProducts = discount.getApplicableProducts() != null && !discount.getApplicableProducts().isEmpty();
        
        if (hasSpecificProducts) {
            applicableProductIds = discount.getApplicableProducts().stream()
                    .map(dp -> dp.getProduct().getId())
                    .collect(Collectors.toSet());
        }
        
        for (int i = 0; i < productIds.size(); i++) {
            Long pid = productIds.get(i);
            BigDecimal subtotal = subtotals.get(i);
            
            if (!hasSpecificProducts || applicableProductIds.contains(pid)) {
                applicableTotal = applicableTotal.add(subtotal);
            }
        }

        if (applicableTotal.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal amount;
        if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
            amount = applicableTotal.multiply(discount.getDiscountAmount()).divide(BigDecimal.valueOf(100));
            if (discount.getMaxDiscountAmount() != null) {
                amount = amount.min(discount.getMaxDiscountAmount());
            }
        } else {
            amount = discount.getDiscountAmount();
        }

        return amount.min(totalAmount);
    }


    private boolean validateDiscount(Discount discount, BigDecimal originalTotalAmount, User user) {
        if (discount.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (originalTotalAmount.compareTo(discount.getMinOrderValue()) < 0) {
            return false;
        }

        if (!discount.isApplicableForUser(user)) {
            return false;
        }

        return discount.isActive() && !discount.isUsageLimitReached();
    }

    private BigDecimal calculateAmount(Discount discount, BigDecimal applicableAmount, BigDecimal orderTotal) {
        BigDecimal amount;
        if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
            amount = applicableAmount.multiply(discount.getDiscountAmount()).divide(BigDecimal.valueOf(100));
            if (discount.getMaxDiscountAmount() != null) {
                amount = amount.min(discount.getMaxDiscountAmount());
            }
        } else {
            amount = discount.getDiscountAmount();
        }
        return amount.min(orderTotal);
    }


    @Override
    public void saveDiscount(String discountCode) {
        User currentUser = getAuthenticatedUser();

        Discount discount = discountRepository.findByCode(discountCode)
                .orElseThrow(() -> new AppException(SocialMarketingErrorCode.DISCOUNT_NOT_FOUND));

        boolean alreadySaved = discountUserRepository.existsByUserIdAndDiscountCode(currentUser.getId(), discountCode);
        if (alreadySaved) {
            throw new AppException(SocialMarketingErrorCode.DISCOUNT_ALREADY_SAVED);
        }

        DiscountUser discountUser = new DiscountUser(discount, currentUser);
        discountUserRepository.save(discountUser);

        discount.setTimesUsed(discount.getTimesUsed() + 1);
        discountRepository.save(discount);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Long> getApplicableProductIds(Long discountId, Pageable pageable) {
        Page<Long> productIdPage = discountProductRepository.findProductIdsByDiscountId(discountId, pageable);
        return toPageResponse(productIdPage, id -> id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Long> getApplicableUserIds(Long discountId, Pageable pageable) {
        Page<Long> userIdPage = discountUserRepository.findUserIdsByDiscountId(discountId, pageable);
        return toPageResponse(userIdPage, id -> id);
    }

    private Discount findDiscountById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new AppException(SocialMarketingErrorCode.DISCOUNT_NOT_FOUND));
    }

    private void addProductsToDiscount(Discount discount, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return;

        List<Product> products = productRepository.findAllById(productIds);

        List<DiscountProduct> discountProducts = products.stream()
                .map(product -> {
                    DiscountProduct discountProduct = new DiscountProduct();
                    discountProduct.setDiscount(discount); // Gán Discount đã được quản lý
                    discountProduct.setProduct(product);
                    return discountProduct;
                })
                .collect(Collectors.toList());

        discountProductRepository.saveAll(discountProducts);
    }


    private void updateDiscountProducts(Discount discount, List<Long> productIds) {
        if (productIds == null) productIds = List.of();

        Set<Long> existingProductIds = discountProductRepository.findByDiscountId(discount.getId())
                .stream()
                .map(dp -> dp.getProduct().getId())
                .collect(Collectors.toSet());

        Set<Long> newProductIds = new HashSet<>(productIds);

        Set<Long> productsToRemove = new HashSet<>(existingProductIds);
        productsToRemove.removeAll(newProductIds);

        Set<Long> productsToAdd = new HashSet<>(newProductIds);
        productsToAdd.removeAll(existingProductIds);

        if (!productsToRemove.isEmpty()) {
            discountProductRepository.deleteByDiscountIdAndProductIds(discount.getId(), productsToRemove);
        }
        addProductsToDiscount(discount, new ArrayList<>(productsToAdd));
    }

    private void updateDiscountUsers(Discount discount, List<Long> userIds) {
        if (userIds == null) userIds = List.of();

        Set<Long> existingUserIds = discountUserRepository.findByDiscountId(discount.getId())
                .stream()
                .map(du -> du.getUser().getId())
                .collect(Collectors.toSet());

        Set<Long> newUserIds = new HashSet<>(userIds);

        Set<Long> usersToRemove = new HashSet<>(existingUserIds);
        usersToRemove.removeAll(newUserIds);

        Set<Long> usersToAdd = new HashSet<>(newUserIds);
        usersToAdd.removeAll(existingUserIds);

        if (!usersToRemove.isEmpty()) {
            discountUserRepository.deleteByDiscountIdAndUserIds(discount.getId(), usersToRemove);
        }

        addUsersToDiscount(discount, new ArrayList<>(usersToAdd));
    }

    private void addUsersToDiscount(Discount discount, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        List<User> users = userRepository.findAllById(userIds);
        List<DiscountUser> discountUsers = users.stream()
                .map(user -> {
                    DiscountUser discountUser = new DiscountUser();
                    discountUser.setDiscount(discount);
                    discountUser.setUser(user);
                    return discountUser;
                })
                .collect(Collectors.toList());
        discountUserRepository.saveAll(discountUsers);
    }
}


