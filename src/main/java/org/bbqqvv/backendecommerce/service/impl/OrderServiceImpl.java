package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.OrderItemRequest;
import org.bbqqvv.backendecommerce.dto.request.OrderRequest;
import org.bbqqvv.backendecommerce.dto.response.OrderResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.mapper.OrderMapper;
import org.bbqqvv.backendecommerce.repository.*;
import org.bbqqvv.backendecommerce.service.OrderService;
import org.bbqqvv.backendecommerce.service.DiscountService;
import org.bbqqvv.backendecommerce.service.email.EmailService;
import org.bbqqvv.backendecommerce.service.payment.PaymentService;
import org.bbqqvv.backendecommerce.util.PagingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final SizeProductVariantRepository sizeProductVariantRepository;
    private final DiscountService discountService;
    private final DiscountRepository discountRepository;
    private final OrderMapper orderMapper;
    private final EmailService emailService;
    private final PaymentService paymentService;

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = BigDecimal.valueOf(499000);
    private static final int EXPECTED_DELIVERY_DAYS = 5;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                            UserRepository userRepository, ProductRepository productRepository, CartRepository cartRepository,
                            AddressRepository addressRepository, SizeProductVariantRepository sizeProductVariantRepository,
                            DiscountService discountService, DiscountRepository discountRepository, OrderMapper orderMapper, EmailService emailService, 
                            PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.sizeProductVariantRepository = sizeProductVariantRepository;
        this.discountService = discountService;
        this.discountRepository = discountRepository;
        this.orderMapper = orderMapper;
        this.emailService = emailService;
        this.paymentService = paymentService;
    }

    private User getAuthenticatedUser() {
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(CommonErrorCode.UNAUTHENTICATED));

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderResponse createOrder(OrderRequest orderRequest) {
        User user = getAuthenticatedUser();
        Address address = findAddressById(orderRequest.getAddressId());

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(CartOrderErrorCode.CART_NOT_FOUND));
        if (cart.getCartItems().isEmpty()) {
            throw new AppException(CartOrderErrorCode.EMPTY_CART);
        }

        // Pre-fetch all needed variants to avoid N+1 queries
        List<Long> productIds = cart.getCartItems().stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());
        
        List<SizeProductVariant> allVariants = sizeProductVariantRepository.findByProductIdIn(productIds);
        Map<String, SizeProductVariant> variantMap = allVariants.stream()
                .collect(Collectors.toMap(
                    v -> v.getProductVariant().getProduct().getId() + ":" + v.getProductVariant().getColor() + ":" + v.getSizeProduct().getSizeName(),
                    v -> v,
                    (v1, v2) -> v1 // Handle duplicates if any
                ));

        // Tính toán giá trị đơn hàng
        BigDecimal orderTotal = cart.getCartItems().stream()
                .map(cartItem -> {
                    String key = cartItem.getProduct().getId() + ":" + cartItem.getColor() + ":" + cartItem.getSizeName();
                    SizeProductVariant sizeProductVariant = variantMap.get(key);
                    if (sizeProductVariant == null) {
                        throw new AppException(ProductErrorCode.SIZE_NOT_FOUND);
                    }
                    return sizeProductVariant.getSizeProduct().getPriceAfterDiscount()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Discount discount = findAndValidateDiscount(orderRequest.getDiscountCode(), orderTotal, user);
        
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discount != null) {
            List<Long> itemProductIds = cart.getCartItems().stream().map(item -> item.getProduct().getId()).toList();
            List<BigDecimal> subtotals = cart.getCartItems().stream().map(CartItem::getSubtotal).toList();
            
            discountAmount = discountService.calculateDiscountAmount(discount, itemProductIds, subtotals, orderTotal);
            
            discount.setTimesUsed(discount.getTimesUsed() + 1);
        }

        BigDecimal totalAfterDiscount = orderTotal.subtract(discountAmount).max(BigDecimal.ZERO);
        BigDecimal shippingFee = calculateShippingFee(address, totalAfterDiscount);
        BigDecimal finalTotalAmount = totalAfterDiscount.add(shippingFee);

        // Tạo và lưu order
        Order order = buildOrder(orderRequest, user, address, shippingFee, discount, discountAmount, finalTotalAmount);
        Order savedOrder = orderRepository.save(order);

        // Tạo và lưu orderItems
        List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(cartItem -> {
                    String key = cartItem.getProduct().getId() + ":" + cartItem.getColor() + ":" + cartItem.getSizeName();
                    SizeProductVariant sizeProductVariant = variantMap.get(key);
                    
                    OrderItemRequest itemRequest = new OrderItemRequest();
                    itemRequest.setQuantity(cartItem.getQuantity());
                    itemRequest.setColor(cartItem.getColor());

                    return buildOrderItem(savedOrder, sizeProductVariant.getProductVariant().getProduct(), sizeProductVariant, itemRequest);
                }).collect(Collectors.toList());
        
        orderItemRepository.saveAll(orderItems);
        savedOrder.setOrderItems(orderItems);
        cartRepository.deleteByUserId(user.getId());

        // 💳 Tạo Link thanh toán (Ví dụ: VNPay) nếu cần
        String paymentUrl = paymentService.createPaymentUrl(savedOrder);
        
        // Gửi email bất đồng bộ
        emailService.sendOrderConfirmationEmail(savedOrder, user.getEmail());
        
        OrderResponse response = orderMapper.toOrderResponse(savedOrder);
        response.setPaymentUrl(paymentUrl);
        return response;
    }

    private Discount findAndValidateDiscount(String discountCode, BigDecimal totalAmount, User user) {
        if (discountCode == null || discountCode.isBlank()) {
            log.info("Không có mã giảm giá");
            return null;
        }

        // Lấy discount từ DB
        Discount discount = discountService.getDiscountByCode(discountCode); // Cần thêm method này vào service

        if (discount == null || !discount.isActive()) {
            log.warn("Mã giảm giá không hợp lệ hoặc chưa được kích hoạt: {}", discountCode);
            return null;
        }

        // 🛡️ Security Check: Hết hạn
        if (discount.isExpired()) {
            log.warn("Mã giảm giá đã hết hạn: {}", discountCode);
            return null;
        }

        // 🛡️ Security Check: Hết lượt sử dụng
        if (discount.isUsageLimitReached()) {
            log.warn("Mã giảm giá đã đạt giới hạn sử dụng: {}", discountCode);
            return null;
        }

        // 🛡️ Security Check: Không dành cho User này (Nếu có giới hạn User)
        if (!discount.isApplicableForUser(user)) {
            log.warn("User {} không thuộc danh sách được áp dụng mã {}", user.getUsername(), discountCode);
            return null;
        }

        // Kiểm tra điều kiện áp dụng mã giảm giá (Giá trị tối thiểu)
        if (totalAmount.compareTo(discount.getMinOrderValue()) < 0) {
            log.warn("Tổng giá trị đơn hàng ({}) không đủ để áp dụng mã giảm giá {}", totalAmount, discountCode);
            return null;
        }

        log.info("Áp dụng mã giảm giá: {}, Giá trị giảm: {}", discountCode, discount.getDiscountAmount());
        return discount;
    }


    private static final Map<String, BigDecimal> SHIPPING_FEES = Map.of(
            "hồ chí minh", BigDecimal.valueOf(50000),
            "hà nội", BigDecimal.valueOf(50000),
            "đà nẵng", BigDecimal.valueOf(20000)
    );

    private BigDecimal calculateShippingFee(Address address, BigDecimal orderTotal) {
        if (orderTotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }

        String province = address.getProvince().trim().toLowerCase();
        return SHIPPING_FEES.getOrDefault(province, BigDecimal.valueOf(50000));
    }

    private Order buildOrder(OrderRequest orderRequest, User user, Address address,
                             BigDecimal shippingFee, Discount discount, BigDecimal discountAmount,
                             BigDecimal totalAmount) {
        Order.OrderBuilder builder = Order.builder()
                .user(user)
                .orderCode(generateOrderCode())
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .fullAddress(address.getFullAddress())
                .status(OrderStatus.PENDING)
                .expectedDeliveryDate(LocalDate.now().plusDays(EXPECTED_DELIVERY_DAYS))
                .paymentMethod(orderRequest.getPaymentMethod())
                .shippingFee(shippingFee)
                .notes(address.getNote())
                .discount(discount)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount);

        if (discount != null) {
            builder.discountCode(discount.getCode());
        }

        return builder.build();
    }

    private String generateOrderCode() {
        return "ORD-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderItem buildOrderItem(Order order, Product product, SizeProductVariant sizeProductVariant, OrderItemRequest itemRequest) {
        SizeProduct sizeProduct = sizeProductVariant.getSizeProduct();
        validateStock(sizeProduct, itemRequest.getQuantity());
        updateStock(sizeProductVariant, itemRequest.getQuantity());

        return OrderItem.builder()
                .order(order)
                .product(product)
                .productVariant(sizeProductVariant.getProductVariant())
                .sizeName(sizeProduct.getSizeName())
                .quantity(itemRequest.getQuantity())
                .price(sizeProduct.getPriceAfterDiscount()) // Sử dụng giá sau giảm
                .subtotal(sizeProduct.getPriceAfterDiscount().multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                .color(itemRequest.getColor())
                .build();
    }
    private void validateStock(SizeProduct sizeProduct, int quantity) {
        if (sizeProduct.getStockQuantity() < quantity) {
            throw new AppException(ProductErrorCode.OUT_OF_STOCK);
        }
    }

    private Address findAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(InfrastructureAddressErrorCode.ADDRESS_NOT_FOUND)); // ✅ Ném lỗi nếu không tìm thấy
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    private SizeProductVariant findSizeProduct(Product product, String sizeName) {
        log.info("Finding SizeProductVariant for product: {} with size: {}", product.getId(), sizeName);

        return sizeProductVariantRepository.findByProductIdAndSizeName(product.getId(), sizeName)
                .orElseThrow(() -> new AppException(ProductErrorCode.SIZE_NOT_FOUND));
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(CartOrderErrorCode.ORDER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        Order order = findOrderByCode(orderCode);  // Sử dụng phương thức tìm kiếm theo mã đơn hàng
        User user = getAuthenticatedUser();

        // Chỉ cho phép admin hoặc chủ sở hữu đơn hàng truy cập
        if (!user.getId().equals(order.getUser().getId()) && !user.isAdmin()) {
            throw new AppException(CommonErrorCode.ACCESS_DENIED);
        }

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public boolean isProductDeliveredToCurrentUser(Long productId) {
        User user = getAuthenticatedUser();
        return orderRepository.existsByUserAndProductAndDelivered(
                user.getId(),
                productId
        );
    }
    private Order findOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(CartOrderErrorCode.ORDER_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByUser(Pageable pageable) {
        User user = getAuthenticatedUser();
        Page<Order> orderPage = orderRepository.findByUserId(user.getId(), pageable);
        return PagingUtil.toPageResponse(orderPage, orderMapper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return PagingUtil.toPageResponse(orderPage, orderMapper::toOrderResponse);
    }


    @Override
    @Transactional
    public OrderResponse updateOrder(Long orderId, OrderRequest orderRequest) {
        Order order = findOrderById(orderId);
        User user = getAuthenticatedUser();

        // 🛡️ Security Check: Chỉ chủ nhân hoặc Admin mới được sửa
        if (!user.getId().equals(order.getUser().getId()) && !user.isAdmin()) {
            throw new AppException(CommonErrorCode.ACCESS_DENIED);
        }

        updateOrderDetails(order, orderRequest);
        return orderMapper.toOrderResponse(order);
    }

    private void updateOrderDetails(Order order, OrderRequest orderRequest) {
        order.setPaymentMethod(orderRequest.getPaymentMethod());

        orderRepository.save(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = findOrderById(orderId);

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new AppException(CartOrderErrorCode.INVALID_ORDER_STATUS);
        }

        orderRepository.save(order);

        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = findOrderById(orderId);
        User user = getAuthenticatedUser();
        if (!user.getId().equals(order.getUser().getId()) && !user.isAdmin()) {
            throw new AppException(CommonErrorCode.ACCESS_DENIED);
        }
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELED) {
            throw new AppException(CartOrderErrorCode.CANNOT_CANCEL_ORDER);
        }


        // 🔄 Hoàn lại số lượng sản phẩm
        List<SizeProductVariant> updatedVariants = new ArrayList<>();
        order.getOrderItems().forEach(item -> {
            SizeProductVariant sizeProductVariant = findSizeProduct(item.getProduct(), item.getSizeName());
            sizeProductVariant.setStock(sizeProductVariant.getStock() + item.getQuantity());
            updatedVariants.add(sizeProductVariant);
        });
        sizeProductVariantRepository.saveAll(updatedVariants);


        // 🔄 Hoàn lại mã giảm giá nếu có
        if (order.getDiscount() != null) {
            Discount discount = order.getDiscount();
            discount.setTimesUsed(Math.max(0, discount.getTimesUsed() - 1));
            discountRepository.save(discount);
            log.info("Refunded coupon {} for canceled order {}", discount.getCode(), order.getOrderCode());
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }


    private void updateStock(SizeProductVariant sizeProductVariant, int quantity) {
        if (sizeProductVariant.getStock() < quantity) {
            throw new AppException(ProductErrorCode.OUT_OF_STOCK);
        }
        sizeProductVariant.setStock(sizeProductVariant.getStock() - quantity);
        sizeProductVariantRepository.save(sizeProductVariant);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = findOrderById(orderId);
        orderItemRepository.deleteAllByOrderId(orderId);
        orderRepository.delete(order);
    }
}

