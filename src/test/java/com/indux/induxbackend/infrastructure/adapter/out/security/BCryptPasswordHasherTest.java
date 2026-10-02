package com.indux.induxbackend.infrastructure.adapter.out.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    @Test
    void elHashNoEsLaContrasenaYSeVerificaCorrectamente() {
        String hash = hasher.hash("secreto123");

        assertThat(hash).isNotEqualTo("secreto123");
        assertThat(hasher.coincide("secreto123", hash)).isTrue();
        assertThat(hasher.coincide("otraClave", hash)).isFalse();
    }
}
