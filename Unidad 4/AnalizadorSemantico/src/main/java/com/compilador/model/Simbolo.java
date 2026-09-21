package com.compilador.model;

public class Simbolo {

    private final String nombre;
    private final TipoDato tipo;
    private String valor;
    private final int direccion;
    private final int linea;
    private boolean inicializada;

    public Simbolo(
            String nombre,
            TipoDato tipo,
            String valor,
            int direccion,
            int linea,
            boolean inicializada
    ) {

        this.nombre = nombre;
        this.tipo = tipo;
        this.valor = valor;
        this.direccion = direccion;
        this.linea = linea;
        this.inicializada = inicializada;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoDato getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public int getDireccion() {
        return direccion;
    }

    public int getLinea() {
        return linea;
    }

    public boolean isInicializada() {
        return inicializada;
    }

    public void setInicializada(boolean inicializada) {
        this.inicializada = inicializada;
    }
}