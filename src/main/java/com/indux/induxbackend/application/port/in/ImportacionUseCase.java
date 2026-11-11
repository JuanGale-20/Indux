package com.indux.induxbackend.application.port.in;

import com.indux.induxbackend.domain.model.ResultadoImportacion;

import java.io.InputStream;

public interface ImportacionUseCase {

    ResultadoImportacion importar(InputStream contenido, String nombreArchivo, Long empresaId,
                                  Long creadoPor, boolean confirmar);

    byte[] generarPlantilla(String formato);
}
