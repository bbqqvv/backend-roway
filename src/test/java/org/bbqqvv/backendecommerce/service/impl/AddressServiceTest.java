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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
        address = Address.builder().id(101L).user(user).defaultAddress(false).build();
        
        addressRequest = new AddressRequest();
        addressRequest.setProvince("Hồ Chí Minh");
        addressRequest.setDefaultAddress(true);
    }

    @Test
    @DisplayName("Tạo địa chỉ mặc định mới - Phải reset các địa chỉ cũ")
    void createAddress_shouldResetOldDefaults_whenNewIsDefault() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            
            Address newAddress = new Address();
            when(addressMapper.toAddress(any())).thenReturn(newAddress);
            when(addressRepository.findAllByUserId(1L)).thenReturn(List.of(address));
            when(addressRepository.save(any())).thenReturn(newAddress);
            when(addressMapper.toAddressResponse(any())).thenReturn(new AddressResponse());

            addressService.createAddress(addressRequest);

            assertThat(address.isDefaultAddress()).isFalse();
            verify(addressRepository).saveAll(anyList());
            verify(addressRepository).save(newAddress);
        }
    }

    @Test
    @DisplayName("Xóa địa chỉ thất bại - Không được xóa địa chỉ mặc định")
    void deleteAddress_shouldThrowException_whenDefault() {
        address.setDefaultAddress(true);
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(addressRepository.findByIdAndUserId(101L, 1L)).thenReturn(Optional.of(address));

            assertThatThrownBy(() -> addressService.deleteAddress(101L))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", InfrastructureAddressErrorCode.ADDRESS_DEFAULT_CANNOT_DELETE);
        }
    }

    @Test
    @DisplayName("Thiết lập địa chỉ mặc định thành công")
    void setDefaultAddress_shouldUpdateFlags() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("testuser"));
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(addressRepository.findByIdAndUserId(101L, 1L)).thenReturn(Optional.of(address));
            when(addressRepository.findAllByUserId(1L)).thenReturn(List.of(address));

            addressService.setDefaultAddress(101L);

            assertThat(address.isDefaultAddress()).isTrue();
            verify(addressRepository).saveAll(anyList());
            verify(addressRepository).save(address);
        }
    }
}
