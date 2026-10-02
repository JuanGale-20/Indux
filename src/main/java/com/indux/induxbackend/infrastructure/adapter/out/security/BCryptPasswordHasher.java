package com.indux.induxbackend.infrastructure.adapter.out.security;

import com.indux.induxbackend.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasherPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String contrasenaPlana) {
        return encoder.encode(contrasenaPlana);
    }

    @Override
    public boolean coincide(String contrasenaPlana, String hash) {
        return encoder.matches(contrasenaPlana, hash);
    }
}