package org.bbqqvv.backendecommerce.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.ApiResponse;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.FavouriteRequest;
import org.bbqqvv.backendecommerce.dto.response.FavouriteResponse;
import org.bbqqvv.backendecommerce.service.FavouriteService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/favourites")
@RequiredArgsConstructor
public class FavouriteController {
    private final FavouriteService favouriteService;

    @PostMapping
    public ApiResponse<FavouriteResponse> addFavourite(@RequestBody @Valid FavouriteRequest favouriteRequest) {
        FavouriteResponse favouriteResponse = favouriteService.addFavourite(favouriteRequest.getProductId());
        return ApiResponse.<FavouriteResponse>builder()
                .success(true)
                .data(favouriteResponse)
                .message("Product added to favourites successfully.")
                .build();
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<String> removeFavourite(@PathVariable Long productId) {
        favouriteService.removeFavourite(productId);
        return ApiResponse.<String>builder()
                .success(true)
                .data("Favourite successfully removed.")
                .message("The product has been removed from favourites.")
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<FavouriteResponse>> getUserFavourites(@PageableDefault(size = 10) Pageable pageable) {
        PageResponse<FavouriteResponse> favouriteResponses = favouriteService.getUserFavourites(pageable);
        return ApiResponse.<PageResponse<FavouriteResponse>>builder()
                .success(true)
                .data(favouriteResponses)
                .message("Paged list of user favourites retrieved successfully.")
                .build();
    }
}
