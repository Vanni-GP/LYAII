package com.compilador.model;

public enum TipoDato {

    INT,
    FLOAT,
    DOUBLE,
    BOOLEAN,
    STRING,
    DESCONOCIDO;

    @Override
    public String toString() {

        return switch (this) {
            case INT -> "int";
            case FLOAT -> "float";
            case DOUBLE -> "double";
            case BOOLEAN -> "boolean";
            case STRING -> "String";
            case DESCONOCIDO -> "desconocido";
        };
    }

    public static TipoDato desdeTexto(String tipo) {

        return switch (tipo) {
            case "int" -> INT;
            case "float" -> FLOAT;
            case "double" -> DOUBLE;
            case "boolean" -> BOOLEAN;
            case "String" -> STRING;
            default -> DESCONOCIDO;
        };
    }

    public boolean esNumerico() {

        return this == INT
                || this == FLOAT
                || this == DOUBLE;
    }
}