package com.indux.induxbackend.application.port.out;

import com.indux.induxbackend.domain.model.Usuario;

public interface TokenProviderPort {

    String generar(Usuario usuario);

    long segundosDeVigencia();
}
