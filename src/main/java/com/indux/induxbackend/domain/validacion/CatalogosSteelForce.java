package com.indux.induxbackend.domain.validacion;

public final class CatalogosSteelForce {

    private CatalogosSteelForce() {
    }

    // PROVISIONAL: el diccionario de SteelForce dice "kg, ton, unidades, etc."
    public static final Catalogo UNIDADES = Catalogo.de("kg", "ton", "unidad", "unidades", "lote");

    // PENDIENTE: SteelForce no lista los sectores válidos.
    // Mientras esté vacío, la regla del sector no se aplica.
    public static final Catalogo SECTORES = Catalogo.vacio();
}
