package org.bbqqvv.backendecommerce.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountTest {

    private Discount discount;
    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        discount = new Discount();
        testUser = User.builder().id(1L).username("testuser").build();
        testProduct = Product.builder().id(100L).name("Test Product").build();
    }

    @Test
    @DisplayName("isApplicableForUser - Danh sách trống (Global) -> Trả về true")
    void isApplicableForUser_shouldReturnTrue_whenUserListIsEmpty() {
        discount.setApplicableUsers(new ArrayList<>());
        assertThat(discount.isApplicableForUser(testUser)).isTrue();
        
        discount.setApplicableUsers(null);
        assertThat(discount.isApplicableForUser(testUser)).isTrue();
    }

    @Test
    @DisplayName("isApplicableForUser - User trong danh sách -> Trả về true")
    void isApplicableForUser_shouldReturnTrue_whenUserIsInList() {
        DiscountUser du = new DiscountUser();
        du.setUser(testUser);
        discount.setApplicableUsers(List.of(du));

        assertThat(discount.isApplicableForUser(testUser)).isTrue();
    }

    @Test
    @DisplayName("isApplicableForUser - User KHÔNG trong danh sách -> Trả về false")
    void isApplicableForUser_shouldReturnFalse_whenUserIsNotInList() {
        User otherUser = User.builder().id(2L).username("other").build();
        DiscountUser du = new DiscountUser();
        du.setUser(otherUser);
        discount.setApplicableUsers(List.of(du));

        assertThat(discount.isApplicableForUser(testUser)).isFalse();
    }

    @Test
    @DisplayName("isApplicableForProduct - Danh sách trống (Global) -> Trả về true")
    void isApplicableForProduct_shouldReturnTrue_whenProductListIsEmpty() {
        discount.setApplicableProducts(new ArrayList<>());
        assertThat(discount.isApplicableForProduct(testProduct)).isTrue();

        discount.setApplicableProducts(null);
        assertThat(discount.isApplicableForProduct(testProduct)).isTrue();
    }

    @Test
    @DisplayName("isApplicableForProduct - Product trong danh sách -> Trả về true")
    void isApplicableForProduct_shouldReturnTrue_whenProductIsInList() {
        DiscountProduct dp = new DiscountProduct();
        dp.setProduct(testProduct);
        discount.setApplicableProducts(List.of(dp));

        assertThat(discount.isApplicableForProduct(testProduct)).isTrue();
    }

    @Test
    @DisplayName("isApplicableForProduct - Product KHÔNG trong danh sách -> Trả về false")
    void isApplicableForProduct_shouldReturnFalse_whenProductIsNotInList() {
        Product otherProduct = Product.builder().id(200L).name("Other").build();
        DiscountProduct dp = new DiscountProduct();
        dp.setProduct(otherProduct);
        discount.setApplicableProducts(List.of(dp));

        assertThat(discount.isApplicableForProduct(testProduct)).isFalse();
    }
}
