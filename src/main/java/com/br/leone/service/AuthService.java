package com.br.leone.service;

import com.br.leone.dto.LoginRequest;
import com.br.leone.entity.User;
import com.br.leone.exception.CredenciaisInvalidasException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserService userService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

   public User autenticar(LoginRequest request) {
        User user = userService.buscarPorEmail(request.email())
                .orElseThrow(() -> new CredenciaisInvalidasException());

        if (!passwordEncoder.matches(request.senha(), user.getSenha())) {
            throw new CredenciaisInvalidasException();
        }
        return user;
   }
}
