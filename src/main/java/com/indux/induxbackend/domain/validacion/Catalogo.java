package com.indux.induxbackend.domain.validacion;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class Catalogo {

    private final Set<String> valores;

    private Catalogo(Set<String> valores) {
        this.valores = Collections.unmodifiableSet(valores);
    }

    public static Catalogo de(String... valores) {
        Set<String> normalizados = new LinkedHashSet<>();
        for (String valor : valores) {
            normalizados.add(normalizar(valor));
        }
        return new Catalogo(normalizados);
    }

    public static Catalogo vacio() {
        return new Catalogo(Set.of());
    }

    public boolean estaVacio() {
        return valores.isEmpty();
    }

    public boolean contiene(String valor) {
        return !Texto.vacio(valor) && valores.contains(normalizar(valor));
    }

    public String descripcion() {
        return String.join(", ", valores);
    }

    private static String normalizar(String valor) {
        return valor.trim().toLowerCase(Locale.ROOT);
    }
}
