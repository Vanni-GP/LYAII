package com.compilador.optimization;

import java.util.ArrayList;
import java.util.List;

public class BloqueBasico {

    private final String id;
    private final List<String> instrucciones;
    private final List<BloqueBasico> sucesores;
    private final List<BloqueBasico> predecesores;

    public BloqueBasico(String id) {
        this.id = id;
        this.instrucciones = new ArrayList<>();
        this.sucesores = new ArrayList<>();
        this.predecesores = new ArrayList<>();
    }

    public void agregarInstruccion(String instruccion) {
        instrucciones.add(instruccion);
    }

    public String getId() {
        return id;
    }

    public List<String> getInstrucciones() {
        return instrucciones;
    }

    public List<BloqueBasico> getSucesores() {
        return sucesores;
    }

    public List<BloqueBasico> getPredecesores() {
        return predecesores;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(id).append(" ===\n");
        for (String inst : instrucciones) {
            sb.append("  ").append(inst).append("\n");
        }
        return sb.toString();
    }
}