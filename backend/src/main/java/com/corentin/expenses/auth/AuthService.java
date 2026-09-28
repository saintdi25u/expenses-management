package com.corentin.expenses.auth;

import com.corentin.expenses.api.dto.AuthResponseDto;
import com.corentin.expenses.api.dto.LoginRequestDto;
import com.corentin.expenses.api.dto.RegisterRequestDto;
import com.corentin.expenses.entity.UserEntity;
import com.corentin.expenses.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public AuthResponseDto register(RegisterRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(requestDto.getEmail());
        userEntity.setFirstName(requestDto.getFirstName());
        userEntity.setLastName(requestDto.getLastName());
        userEntity.setPassword_hash(passwordEncoder.encode(requestDto.getPassword()));
        userRepository.save(userEntity);
        return new AuthResponseDto().token(tokenService.generate(userEntity));
    }


    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        UserEntity user = userRepository.findByEmail(loginRequestDto.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDto.getPassword(), u.getPassword_hash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid identifier"));
        return new AuthResponseDto(tokenService.generate(user));
    }
}
