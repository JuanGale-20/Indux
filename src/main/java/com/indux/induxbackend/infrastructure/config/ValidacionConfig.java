package com.indux.induxbackend.infrastructure.config;

import com.indux.induxbackend.domain.validacion.CatalogosSteelForce;
import com.indux.induxbackend.domain.validacion.ValidadorEmpresa;
import com.indux.induxbackend.domain.validacion.ValidadorProceso;
import com.indux.induxbackend.domain.validacion.ValidadorProducto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidacionConfig {

    @Bean
    ValidadorEmpresa validadorEmpresa() {
        return new ValidadorEmpresa(CatalogosSteelForce.SECTORES);
    }

    @Bean
    ValidadorProducto validadorProducto() {
        return new ValidadorProducto(CatalogosSteelForce.UNIDADES);
    }

    @Bean
    ValidadorProceso validadorProceso() {
        return new ValidadorProceso(CatalogosSteelForce.UNIDADES);
    }
}
