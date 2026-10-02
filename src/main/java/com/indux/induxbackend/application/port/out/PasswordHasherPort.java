package com.indux.induxbackend.application.port.out;

public interface PasswordHasherPort {

    String hash(String contrasenaPlana);

    boolean coincide(String contrasenaPlana, String hash);
}
