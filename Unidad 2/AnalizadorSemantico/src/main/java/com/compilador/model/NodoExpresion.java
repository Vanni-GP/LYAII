package com.compilador.model;

public class NodoExpresion {

    private final String valor;
    private TipoDato tipo;

    private NodoExpresion izquierdo;
    private NodoExpresion derecho;

    public NodoExpresion(
            String valor,
            TipoDato tipo
    ) {
        this.valor = valor;
        this.tipo = tipo;
    }

    public NodoExpresion(
            String valor,
            TipoDato tipo,
            NodoExpresion izquierdo,
            NodoExpresion derecho
    ) {
        this.valor = valor;
        this.tipo = tipo;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }

    public String getValor() {
        return valor;
    }

    public TipoDato getTipo() {
        return tipo;
    }

    public void setTipo(TipoDato tipo) {
        this.tipo = tipo;
    }

    public NodoExpresion getIzquierdo() {
        return izquierdo;
    }

    public NodoExpresion getDerecho() {
        return derecho;
    }

    public boolean esHoja() {
        return izquierdo == null
                && derecho == null;
    }

    @Override
    public String toString() {
        return valor + " : " + tipo;
    }
}