package com.br.leone.controller;

import com.br.leone.dto.LoginRequest;
import com.br.leone.dto.LoginResponse;
import com.br.leone.entity.User;
import com.br.leone.security.JwtService;
import com.br.leone.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        User user = authService.autenticar(request);

        String token = jwtService.gerarToken(user);


        return ResponseEntity.ok(new LoginResponse(token));
    }
}
