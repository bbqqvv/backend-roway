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
@Tag(name = "Address Management", description = "Quản lý địa chỉ nhận hàng của người dùng.")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @Operation(summary = "Thêm địa chỉ mới", description = "Thêm một địa chỉ nhận hàng mới vào danh mục của người dùng hiện tại.")
    public ApiResponse<AddressResponse> createAddress(@RequestBody @Valid AddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .success(true)
                .data(addressService.createAddress(request))
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy danh sách địa chỉ của tôi", description = "Trả về toàn bộ danh sách các địa chỉ nhận hàng mà người dùng hiện tại đã lưu.")
    public ApiResponse<List<AddressResponse>> getMyAddresses() {
        return ApiResponse.<List<AddressResponse>>builder()
                .success(true)
                .data(addressService.getAddressesByUser())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết địa chỉ theo ID", description = "Xem thông tin chi tiết của một địa chỉ cụ thể thông qua mã ID.")
    public ApiResponse<AddressResponse> getAddress(@PathVariable Long id) {
        return ApiResponse.<AddressResponse>builder()
                .success(true)
                .data(addressService.getAddressById(id))
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật địa chỉ", description = "Sửa đổi thông tin của một địa chỉ nhận hàng đã tồn tại.")
    public ApiResponse<AddressResponse> updateAddress(@PathVariable Long id, @RequestBody @Valid AddressRequest request) {
        return ApiResponse.<AddressResponse>builder()
                .success(true)
                .data(addressService.updateAddress(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa địa chỉ", description = "Xóa hoàn toàn một địa chỉ khỏi tài khoản người dùng.")
    public ApiResponse<String> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ApiResponse.<String>builder()
                .success(true)
                .data("Address deleted")
                .build();
    }

    @PutMapping("/{id}/set-default")
    @Operation(summary = "Đặt làm địa chỉ mặc định", description = "Thiết lập một địa chỉ cụ thể làm địa chỉ giao hàng ưu tiên (mặc định) cho các đơn hàng sau này.")
    public ApiResponse<AddressResponse> setDefault(@PathVariable Long id) {
        return ApiResponse.<AddressResponse>builder()
                .success(true)
                .data(addressService.setDefaultAddress(id))
                .build();
    }
}
