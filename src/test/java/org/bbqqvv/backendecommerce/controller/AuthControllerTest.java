package org.bbqqvv.backendecommerce.controller;

import org.bbqqvv.backendecommerce.BaseIntegrationTest;
import org.bbqqvv.backendecommerce.dto.request.AuthenticationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest extends BaseIntegrationTest {

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername("invalid_user");
        request.setPassword("wrong_password");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
