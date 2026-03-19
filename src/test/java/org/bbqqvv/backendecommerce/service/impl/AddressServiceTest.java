package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.request.AddressRequest;
import org.bbqqvv.backendecommerce.dto.response.AddressResponse;
import org.bbqqvv.backendecommerce.entity.Address;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.codes.InfrastructureAddressErrorCode;
import org.bbqqvv.backendecommerce.mapper.AddressMapper;
import org.bbqqvv.backendecommerce.repository.AddressRepository;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock private AddressRepository addressRepository;
    @Mock private AddressMapper addressMapper;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User user;
    private Address address;
    private AddressRequest addressRequest;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();
        address = Address.builder()
                .id(1L)
                .user(user)
                .recipientName("Van Quoc Bui")
                .phoneNumber("0123456789")
                .addressLine("123 Street")
                .defaultAddress(false)
                .build();

        addressRequest = new AddressRequest();
        addressRequest.setRecipientName("Van Quoc Bui");
        addressRequest.setPhoneNumber("0123456789");
        addressRequest.setAddressLine("123 Street");
        addressRequest.setDefaultAddress(false);
    }

    @Test
    @DisplayName("Thêm địa chỉ mới thành công")
    void createAddress_shouldReturnAddressResponse() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(addressMapper.toAddress(any())).thenReturn(address);
            when(addressRepository.save(any())).thenReturn(address);
            when(addressMapper.toAddressResponse(any())).thenReturn(new AddressResponse());

            // Act
            AddressResponse response = addressService.createAddress(addressRequest);

            // Assert
            assertThat(response).isNotNull();
            verify(addressRepository).save(any(Address.class));
        }
    }

    @Test
    @DisplayName("Xóa địa chỉ thất bại - Không thể xóa địa chỉ mặc định")
    void deleteAddress_shouldThrowException_whenDeletingDefaultAddress() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            // Arrange
            address.setDefaultAddress(true);
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(addressRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(address));

            // Act & Assert
            assertThatThrownBy(() -> addressService.deleteAddress(1L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", InfrastructureAddressErrorCode.ADDRESS_DEFAULT_CANNOT_DELETE);
        }
    }
}
