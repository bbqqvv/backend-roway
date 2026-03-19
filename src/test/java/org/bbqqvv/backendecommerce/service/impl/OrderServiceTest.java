package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.request.OrderRequest;
import org.bbqqvv.backendecommerce.dto.response.OrderResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.CartOrderErrorCode;
import org.bbqqvv.backendecommerce.mapper.OrderMapper;
import org.bbqqvv.backendecommerce.repository.*;
import org.bbqqvv.backendecommerce.service.DiscountService;
import org.bbqqvv.backendecommerce.service.email.EmailService;
import org.bbqqvv.backendecommerce.service.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CartRepository cartRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private SizeProductVariantRepository sizeProductVariantRepository;
    @Mock private DiscountService discountService;
    @Mock private DiscountRepository discountRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private EmailService emailService;
    @Mock private PaymentService paymentService;
    @Mock private org.bbqqvv.backendecommerce.service.ShippingService shippingService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Address address;
    private Cart cart;
    private Product product;
    private ProductVariant variant;
    private SizeProductVariant sizeVariant;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").email("test@example.com").build();
        address = Address.builder().id(1L).province("Hồ Chí Minh").build();
        
        product = Product.builder().id(101L).name("Jeans").build();
        variant = ProductVariant.builder().id(201L).product(product).color("Blue").build();
        
        SizeProduct sizeProduct = SizeProduct.builder()
                .sizeName("M")
                .priceAfterDiscount(BigDecimal.valueOf(100000))
                .build();
        
        sizeVariant = SizeProductVariant.builder()
                .id(301L)
                .productVariant(variant)
                .sizeProduct(sizeProduct)
                .stock(10)
                .build();

        sizeProduct.setProductVariantSizes(List.of(sizeVariant));

        CartItem cartItem = CartItem.builder()
                .product(product)
                .color("Blue")
                .sizeName("M")
                .quantity(2)
                .subtotal(BigDecimal.valueOf(200000))
                .build();
        
        cart = Cart.builder().id(1L).user(user).cartItems(List.of(cartItem)).build();
    }

    @Test
    @DisplayName("Tạo đơn hàng thành công")
    void createOrder_shouldSaveOrderAndItems() {
        OrderRequest request = new OrderRequest();
        request.setAddressId(1L);
        request.setPaymentMethod(PaymentMethod.VNPAY);

        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            when(sizeProductVariantRepository.findByProductIdIn(anyList())).thenReturn(List.of(sizeVariant));
            
            Order order = new Order();
            order.setId(1L);
            order.setOrderItems(Collections.emptyList());
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(paymentService.createPaymentUrl(any())).thenReturn("http://payment.url");
            when(orderMapper.toOrderResponse(any())).thenReturn(new OrderResponse());
            when(shippingService.getFreeShippingThreshold()).thenReturn(BigDecimal.valueOf(500000));
            when(shippingService.getShippingFeeByRegion(anyString())).thenReturn(BigDecimal.valueOf(30000));

            OrderResponse response = orderService.createOrder(request);

            assertThat(response).isNotNull();
            verify(orderRepository).save(any(Order.class));
            verify(orderItemRepository).saveAll(anyList());
            verify(cartRepository).deleteByUserId(1L);
            verify(emailService).sendOrderConfirmationEmail(any(), anyString());
        }
    }

    @Test
    @DisplayName("Hủy đơn hàng thành công - Hoàn lại kho")
    void cancelOrder_shouldRestoreStock() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        
        OrderItem item = OrderItem.builder()
                .product(product)
                .sizeName("M")
                .quantity(2)
                .build();
        order.setOrderItems(List.of(item));

        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(sizeProductVariantRepository.findByProductIdAndSizeName(101L, "M")).thenReturn(Optional.of(sizeVariant));

            orderService.cancelOrder(1L);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
            assertThat(sizeVariant.getStock()).isEqualTo(12); // 10 + 2
            verify(orderRepository).save(order);
        }
    }

    @Test
    @DisplayName("Hủy đơn hàng thất bại - Trạng thái không hợp lệ")
    void cancelOrder_shouldThrowException_whenDelivered() {
        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.DELIVERED);

        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", CartOrderErrorCode.CANNOT_CANCEL_ORDER);
        }
    }
}
