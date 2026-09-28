package com.corentin.expenses.auth;

import com.corentin.expenses.api.dto.AuthResponseDto;
import com.corentin.expenses.api.dto.LoginRequestDto;
import com.corentin.expenses.api.dto.RegisterRequestDto;
import com.corentin.expenses.config.JwtProperties;
import com.corentin.expenses.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthDelegate.class,
        properties = "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@Import({SecurityConfig.class, AuthDelegateTest.JwtPropertiesConfig.class})
class AuthDelegateTest {

    @TestConfiguration
    @EnableConfigurationProperties(JwtProperties.class)
    static class JwtPropertiesConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void login_returnsToken() throws Exception {
        when(authService.login(any(LoginRequestDto.class))).thenReturn(new AuthResponseDto("jwt-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "john@doe.com", "password": "secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_returnsUnauthorized_whenServiceRejectsCredentials() throws Exception {
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid identifier"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "john@doe.com", "password": "wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_returnsBadRequest_whenBodyIsIncomplete() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "john@doe.com"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void register_returnsToken() throws Exception {
        when(authService.register(any(RegisterRequestDto.class))).thenReturn(new AuthResponseDto("jwt-token"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "john@doe.com", "password": "secret", "firstName": "John", "lastName": "Doe"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void register_returnsConflict_whenEmailAlreadyExists() throws Exception {
        when(authService.register(any(RegisterRequestDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "john@doe.com", "password": "secret", "firstName": "John", "lastName": "Doe"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void register_returnsBadRequest_whenBodyIsIncomplete() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "john@doe.com", "password": "secret"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}
