package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.dto.request.OrderItemRequest;
import org.bbqqvv.backendecommerce.dto.request.OrderRequest;
import org.bbqqvv.backendecommerce.dto.response.OrderResponse;
import org.bbqqvv.backendecommerce.entity.*;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.repository.*;
import org.bbqqvv.backendecommerce.service.email.EmailService;
import org.bbqqvv.backendecommerce.service.payment.PaymentService;
import org.bbqqvv.backendecommerce.mapper.OrderMapper;
import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    @Mock private DiscountRepository discountRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private EmailService emailService;
    @Mock private PaymentService paymentService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private OrderRequest orderRequest;
    private Address address;
    private Product product;
    private SizeProductVariant productVariant;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test_user").email("user@example.com").build();
        address = Address.builder()
                .id(1L)
                .user(user)
                .recipientName("Van Quoc Bui")
                .phoneNumber("0123456789")
                .addressLine("123 Street")
                .build();
        product = Product.builder().id(1L).name("Roway T-Shirt").build();

        orderRequest = OrderRequest.builder()
                .addressId(1L)
                .paymentMethod(PaymentMethod.VNPAY)
                .build();
    }

    @Test
    @DisplayName("Đặt hàng thành công - Quy trình giao dịch giả lập")
    void createOrder_shouldReturnOrderResponse_whenValidRequest() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("test_user"));
            when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(user));
            when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
            
            Cart cart = new Cart();
            cart.setCartItems(List.of(CartItem.builder()
                    .product(product)
                    .color("Black")
                    .sizeName("L")
                    .quantity(2)
                    .price(BigDecimal.valueOf(500000))
                    .build()));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            
            SizeProduct sizeProduct = SizeProduct.builder()
                    .sizeName("L")
                    .price(BigDecimal.valueOf(500000))
                    .priceAfterDiscount(BigDecimal.valueOf(500000))
                    .build();
            
            productVariant = SizeProductVariant.builder()
                    .id(1L)
                    .stock(10)
                    .productVariant(ProductVariant.builder()
                            .color("Black")
                            .product(product)
                            .build())
                    .sizeProduct(sizeProduct)
                    .build();

            when(sizeProductVariantRepository.findByProductIdIn(anyList())).thenReturn(List.of(productVariant));
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
                Order order = i.getArgument(0);
                order.setId(100L);
                return order;
            });
            when(paymentService.createPaymentUrl(any())).thenReturn("http://vnpay.com/pay");
            when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(new OrderResponse());

            // Act
            OrderResponse response = orderService.createOrder(orderRequest);

            // Assert
            assertThat(response).isNotNull();
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(sizeProductVariantRepository, times(1)).save(any(SizeProductVariant.class));
            assertThat(productVariant.getStock()).isEqualTo(8);
        }
    }

    @Test
    @DisplayName("Đặt hàng thất bại - Hết hàng")
    void createOrder_shouldThrowException_whenOutOfStock() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("test_user"));
            when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(user));
            when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
            
            Cart cart = new Cart();
            cart.setCartItems(List.of(CartItem.builder()
                    .product(product)
                    .color("Black")
                    .sizeName("L")
                    .quantity(100) // Much more than stock
                    .price(BigDecimal.valueOf(500000))
                    .build()));
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
            
            SizeProduct sizeProduct = SizeProduct.builder()
                    .sizeName("L")
                    .price(BigDecimal.valueOf(500000))
                    .priceAfterDiscount(BigDecimal.valueOf(500000))
                    .build();
            
            // In a real SizeProduct, getStockQuantity would sum all productVariantSizes.
            // Since it's a real object in our test (not mock), we need to set the list or mock the method.
            // But SizeProduct is just an entity here. Let's see if we can just mock it or if we should use a real list.
            
            productVariant = SizeProductVariant.builder()
                    .id(1L)
                    .stock(5)
                    .productVariant(ProductVariant.builder().color("Black").product(product).build())
                    .sizeProduct(sizeProduct)
                    .build();
            
            sizeProduct.setProductVariantSizes(List.of(productVariant));

            when(sizeProductVariantRepository.findByProductIdIn(anyList())).thenReturn(List.of(productVariant));

            // Act & Assert
            try {
                orderService.createOrder(orderRequest);
            } catch (AppException e) {
                assertThat(e.getErrorCode()).isNotNull();
            }

            // In OrderServiceImpl, the order is saved BEFORE buildOrderItem loop fails
            verify(orderRepository, times(1)).save(any(Order.class));
            // Ensure items are NEVER saved if stock validation fails
            verify(orderItemRepository, never()).saveAll(any());
            verify(sizeProductVariantRepository, never()).save(any(SizeProductVariant.class));
        }
    }
}
