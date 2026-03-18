package org.bbqqvv.backendecommerce.controller;

import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.request.AddressRequest;
import org.bbqqvv.backendecommerce.dto.response.AddressResponse;
import org.bbqqvv.backendecommerce.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Address", description = "Address API for managing user addresses")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @Operation(summary = "Create new address")
    public ApiResponse<AddressResponse> createAddress(@RequestBody @Valid AddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .success(true)
                .data(addressService.createAddress(request))
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's addresses")
    public ApiResponse<List<AddressResponse>> getMyAddresses() {
        return ApiResponse.<List<AddressResponse>>builder()
                .success(true)
                .data(addressService.getAddressesByUser())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID")
    public ApiResponse<AddressResponse> getAddress(@PathVariable Long id) {
        return ApiResponse.<AddressResponse>builder()
                .success(true)
                .data(addressService.getAddressById(id))
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address by ID")
    public ApiResponse<AddressResponse> updateAddress(@PathVariable Long id, @RequestBody @Valid AddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .success(true)
                .data(addressService.updateAddress(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address by ID")
    public ApiResponse<String> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ApiResponse.<String>builder()
                .success(true)
                .data("Address deleted")
                .build();
    }

    @PutMapping("/{id}/set-default")
    @Operation(summary = "Set address as default")
    public ApiResponse<AddressResponse> setDefault(@PathVariable Long id) {
        return ApiResponse.<AddressResponse>builder()
                .success(true)
                .data(addressService.setDefaultAddress(id))
                .build();
    }
}
