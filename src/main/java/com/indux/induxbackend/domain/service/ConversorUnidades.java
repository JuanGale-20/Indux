package com.indux.induxbackend.domain.service;

import com.indux.induxbackend.domain.exception.DominioException;
import com.indux.induxbackend.domain.model.EquivalenciaUnidad;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ConversorUnidades {

    private static final int ESCALA_CALCULO = 10;
    private static final int ESCALA_RESULTADO = 6;

    private record CandidatoArista(BigDecimal factor, int especificidad) {}

    private final Map<String, Map<String, BigDecimal>> adyacencias = new HashMap<>();

    public ConversorUnidades(List<EquivalenciaUnidad> equivalencias) {
        if (equivalencias != null) {
            construirGrafo(equivalencias);
        }
    }

    private void construirGrafo(List<EquivalenciaUnidad> equivalencias) {
        // Para cada par dirigido (origen, destino), seleccionamos la equivalencia más específica.
        // Especificidad: producto (3) > empresa (2) > global (1).
        Map<String, Map<String, CandidatoArista>> mejores = new HashMap<>();

        for (EquivalenciaUnidad eq : equivalencias) {
            String origen = normalizar(eq.unidadOrigen());
            String destino = normalizar(eq.unidadDestino());
            if (origen.equals(destino)) {
                continue;
            }

            int esp = 1;
            if (eq.productoId() != null) {
                esp = 3;
            } else if (eq.empresaId() != null) {
                esp = 2;
            }

            BigDecimal factorDirecto = eq.factor();
            BigDecimal factorInverso = BigDecimal.ONE.divide(factorDirecto, ESCALA_CALCULO, RoundingMode.HALF_UP);

            // Directo
            actualizarMejor(mejores, origen, destino, factorDirecto, esp);
            // Inverso (con la misma especificidad)
            actualizarMejor(mejores, destino, origen, factorInverso, esp);
        }

        for (Map.Entry<String, Map<String, CandidatoArista>> e1 : mejores.entrySet()) {
            Map<String, BigDecimal> mapa = new HashMap<>();
            for (Map.Entry<String, CandidatoArista> e2 : e1.getValue().entrySet()) {
                mapa.put(e2.getKey(), e2.getValue().factor());
            }
            adyacencias.put(e1.getKey(), mapa);
        }
    }

    private static void actualizarMejor(Map<String, Map<String, CandidatoArista>> mejores,
                                        String origen, String destino, BigDecimal factor, int especificidad) {
        mejores.putIfAbsent(origen, new HashMap<>());
        Map<String, CandidatoArista> destinos = mejores.get(origen);
        CandidatoArista actual = destinos.get(destino);
        if (actual == null || especificidad > actual.especificidad()) {
            destinos.put(destino, new CandidatoArista(factor, especificidad));
        }
    }

    public BigDecimal convertir(BigDecimal valor, String de, String a) {
        if (valor == null) {
            throw new DominioException("El valor a convertir no puede ser nulo");
        }
        String origen = normalizar(de);
        String destino = normalizar(a);

        if (origen.equals(destino)) {
            return valor;
        }

        BigDecimal factor = buscarFactorBFS(origen, destino);
        if (factor == null) {
            throw new DominioException("No existe una equivalencia entre '" + de + "' y '" + a
                    + "'. Define la equivalencia de unidades para poder convertir");
        }
        return valor.multiply(factor).setScale(ESCALA_RESULTADO, RoundingMode.HALF_UP);
    }

    public boolean puedeConvertir(String de, String a) {
        if (de == null || a == null) {
            return false;
        }
        String origen = normalizar(de);
        String destino = normalizar(a);
        if (origen.equals(destino)) {
            return true;
        }
        return buscarFactorBFS(origen, destino) != null;
    }

    private BigDecimal buscarFactorBFS(String origen, String destino) {
        if (!adyacencias.containsKey(origen)) {
            return null;
        }

        record Estado(String nodo, BigDecimal factorAcumulado) {}
        Queue<Estado> cola = new ArrayDeque<>();
        Set<String> visitados = new HashSet<>();

        cola.add(new Estado(origen, BigDecimal.ONE));
        visitados.add(origen);

        while (!cola.isEmpty()) {
            Estado actual = cola.poll();
            if (actual.nodo().equals(destino)) {
                return actual.factorAcumulado();
            }

            Map<String, BigDecimal> vecinos = adyacencias.getOrDefault(actual.nodo(), Collections.emptyMap());
            for (Map.Entry<String, BigDecimal> vecino : vecinos.entrySet()) {
                String siguiente = vecino.getKey();
                if (!visitados.contains(siguiente)) {
                    visitados.add(siguiente);
                    BigDecimal nuevoFactor = actual.factorAcumulado().multiply(vecino.getValue());
                    cola.add(new Estado(siguiente, nuevoFactor));
                }
            }
        }
        return null;
    }

    private static String normalizar(String unidad) {
        if (unidad == null) {
            return "";
        }
        return unidad.trim().toLowerCase();
    }
}
