package com.compilador.model;

public class ErrorSemantico {

    private final int linea;
    private final String codigo;
    private final String mensaje;

    public ErrorSemantico(
            int linea,
            String codigo,
            String mensaje
    ) {

        this.linea = linea;
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public int getLinea() {
        return linea;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    @Override
    public String toString() {

        return "Línea " + linea
                + " [" + codigo + "]: "
                + mensaje;
    }
}