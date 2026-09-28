package com.corentin.expenses.auth;

import com.corentin.expenses.api.AuthApi;
import com.corentin.expenses.api.dto.AuthResponseDto;
import com.corentin.expenses.api.dto.LoginRequestDto;
import com.corentin.expenses.api.dto.RegisterRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class AuthDelegate implements AuthApi {

    private final AuthService authService;

    public AuthDelegate(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<AuthResponseDto> authLogin(LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @Override
    public ResponseEntity<AuthResponseDto> authRegister(RegisterRequestDto registerRequestDto) {
        return ResponseEntity.ok(authService.register(registerRequestDto));
    }


}
