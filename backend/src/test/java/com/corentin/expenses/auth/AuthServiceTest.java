package com.corentin.expenses.auth;

import com.corentin.expenses.api.dto.AuthResponseDto;
import com.corentin.expenses.api.dto.LoginRequestDto;
import com.corentin.expenses.api.dto.RegisterRequestDto;
import com.corentin.expenses.entity.Role;
import com.corentin.expenses.entity.UserEntity;
import com.corentin.expenses.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_savesUserWithEncodedPasswordAndReturnsToken() {
        RegisterRequestDto request = new RegisterRequestDto("john@doe.com", "secret", "Doe", "John");
        when(userRepository.existsByEmail("john@doe.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(tokenService.generate(any(UserEntity.class))).thenReturn("jwt-token");

        AuthResponseDto response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("john@doe.com");
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getPassword_hash()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        verify(tokenService).generate(saved);
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyExists() {
        RegisterRequestDto request = new RegisterRequestDto("john@doe.com", "secret", "Doe", "John");
        when(userRepository.existsByEmail("john@doe.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(userRepository, never()).save(any());
        verify(tokenService, never()).generate(any());
    }

    @Test
    void login_returnsToken_whenCredentialsAreValid() {
        UserEntity user = user("john@doe.com", "hashed");
        when(userRepository.findByEmail("john@doe.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(tokenService.generate(user)).thenReturn("jwt-token");

        AuthResponseDto response = authService.login(new LoginRequestDto("john@doe.com", "secret"));

        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_throwsUnauthorized_whenPasswordDoesNotMatch() {
        UserEntity user = user("john@doe.com", "hashed");
        when(userRepository.findByEmail("john@doe.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequestDto("john@doe.com", "wrong")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        verify(tokenService, never()).generate(any());
    }

    @Test
    void login_throwsUnauthorized_whenUserDoesNotExist() {
        when(userRepository.findByEmail("unknown@doe.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequestDto("unknown@doe.com", "secret")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenService, never()).generate(any());
    }

    private static UserEntity user(String email, String passwordHash) {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword_hash(passwordHash);
        return user;
    }
}
