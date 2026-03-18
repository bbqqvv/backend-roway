package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.request.AddressRequest;
import org.bbqqvv.backendecommerce.dto.response.AddressResponse;
import org.bbqqvv.backendecommerce.entity.Address;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.mapper.AddressMapper;
import org.bbqqvv.backendecommerce.repository.AddressRepository;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.bbqqvv.backendecommerce.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressServiceImpl implements AddressService {

    AddressRepository addressRepository;
    AddressMapper addressMapper;
    UserRepository userRepository;

    public AddressServiceImpl(AddressRepository addressRepository, AddressMapper addressMapper, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AddressResponse createAddress(AddressRequest addressRequest) {
        User user = getAuthenticatedUser();
        Address address = addressMapper.toAddress(addressRequest);
        address.setUser(user);

        if (addressRequest.isDefaultAddress()) {
            log.info("Setting new default address for user {}", user.getId());
            setDefaultAddressForUser(user, null);
            address.setDefaultAddress(true);
        }

        return addressMapper.toAddressResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest addressRequest) {
        Address existingAddress = findAddressByIdAndUser(addressId);
        addressMapper.updateEntityFromRequest(addressRequest, existingAddress);

        if (addressRequest.isDefaultAddress() && !existingAddress.isDefaultAddress()) {
            setDefaultAddressForUser(existingAddress.getUser(), addressId);
            existingAddress.setDefaultAddress(true);
        }

        return addressMapper.toAddressResponse(addressRepository.save(existingAddress));
    }

    @Override
    public List<AddressResponse> getAddressesByUser() {
        User user = getAuthenticatedUser();
        return addressRepository.findAllByUserId(user.getId()).stream()
                .map(addressMapper::toAddressResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponse getAddressById(Long addressId) {
        return addressMapper.toAddressResponse(findAddressByIdAndUser(addressId));
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        Address address = findAddressByIdAndUser(addressId);

        if (address.isDefaultAddress()) {
            throw new AppException(InfrastructureAddressErrorCode.ADDRESS_DEFAULT_CANNOT_DELETE);
        }

        addressRepository.delete(address);
    }



    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long addressId) {
        Address address = findAddressByIdAndUser(addressId);
        setDefaultAddressForUser(address.getUser(), addressId);
        return addressMapper.toAddressResponse(addressRepository.save(address));
    }

    private User getAuthenticatedUser() {
        return SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findByUsername)
                .orElseThrow(() -> new AppException(CommonErrorCode.UNAUTHENTICATED));
    }

    private Address findAddressByIdAndUser(Long addressId) {
        User user = getAuthenticatedUser();
        return addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new AppException(InfrastructureAddressErrorCode.ADDRESS_NOT_FOUND));
    }

    private void setDefaultAddressForUser(User user, Long newDefaultAddressId) {
        List<Address> userAddresses = addressRepository.findAllByUserId(user.getId());

        userAddresses.forEach(addr -> addr.setDefaultAddress(addr.getId().equals(newDefaultAddressId)));

        addressRepository.saveAll(userAddresses);
    }
}

