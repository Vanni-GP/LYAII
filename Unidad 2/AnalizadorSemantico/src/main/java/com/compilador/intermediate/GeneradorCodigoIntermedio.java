package com.compilador.intermediate;

import com.compilador.model.NodoExpresion;

import java.util.ArrayList;
import java.util.List;

public class GeneradorCodigoIntermedio {

    private int contadorTemporales;

    private final List<String> codigoTresDirecciones;
    private final List<String> codigoP;
    private final List<Triplo> triplos;
    private final List<Cuadruplo> cuadruplos;

    public GeneradorCodigoIntermedio() {

        codigoTresDirecciones = new ArrayList<>();
        codigoP = new ArrayList<>();
        triplos = new ArrayList<>();
        cuadruplos = new ArrayList<>();

        contadorTemporales = 1;
    }

    // =========================================================
    // LIMPIAR
    // =========================================================

    public void limpiar() {

        contadorTemporales = 1;

        codigoTresDirecciones.clear();
        codigoP.clear();
        triplos.clear();
        cuadruplos.clear();
    }

    // =========================================================
    // INFIJA
    // =========================================================

    public String generarInfija(NodoExpresion nodo) {

        if (nodo == null) {
            return "";
        }

        if (nodo.esHoja()) {
            return nodo.getValor();
        }

        String izquierdo =
                generarInfija(
                        nodo.getIzquierdo()
                );

        String derecho =
                generarInfija(
                        nodo.getDerecho()
                );

        return "("
                + izquierdo
                + " "
                + nodo.getValor()
                + " "
                + derecho
                + ")";
    }

    // =========================================================
    // PREFIJA
    // =========================================================

    public String generarPrefija(NodoExpresion nodo) {

        if (nodo == null) {
            return "";
        }

        if (nodo.esHoja()) {
            return nodo.getValor();
        }

        String izquierdo =
                generarPrefija(
                        nodo.getIzquierdo()
                );

        String derecho =
                generarPrefija(
                        nodo.getDerecho()
                );

        return nodo.getValor()
                + " "
                + izquierdo
                + " "
                + derecho;
    }

    // =========================================================
    // POSTFIJA
    // =========================================================

    public String generarPostfija(NodoExpresion nodo) {

        if (nodo == null) {
            return "";
        }

        if (nodo.esHoja()) {
            return nodo.getValor();
        }

        String izquierdo =
                generarPostfija(
                        nodo.getIzquierdo()
                );

        String derecho =
                generarPostfija(
                        nodo.getDerecho()
                );

        return izquierdo
                + " "
                + derecho
                + " "
                + nodo.getValor();
    }

    // =========================================================
    // NOTACIÓN POLACA
    // =========================================================
    //
    // La notación Polaca utiliza la forma prefija.
    // =========================================================

    public String generarPolaca(NodoExpresion nodo) {

        return generarPrefija(nodo);
    }

    // =========================================================
    // CÓDIGO DE TRES DIRECCIONES
    // =========================================================

    public String generarTresDirecciones(
            NodoExpresion nodo
    ) {

        limpiar();

        if (nodo == null) {
            return "";
        }

        generarTresDireccionesRecursivo(nodo);

        return String.join(
                "\n",
                codigoTresDirecciones
        );
    }

    private String generarTresDireccionesRecursivo(
            NodoExpresion nodo
    ) {

        if (nodo.esHoja()) {
            return nodo.getValor();
        }

        String izquierdo =
                generarTresDireccionesRecursivo(
                        nodo.getIzquierdo()
                );

        String derecho =
                generarTresDireccionesRecursivo(
                        nodo.getDerecho()
                );

        String temporal =
                nuevoTemporal();

        codigoTresDirecciones.add(
                temporal
                        + " = "
                        + izquierdo
                        + " "
                        + nodo.getValor()
                        + " "
                        + derecho
        );

        return temporal;
    }

    // =========================================================
    // CÓDIGO P
    // =========================================================

    public String generarCodigoP(
            NodoExpresion nodo
    ) {

        codigoP.clear();

        if (nodo == null) {
            return "";
        }

        generarCodigoPRecursivo(nodo);

        return String.join(
                "\n",
                codigoP
        );
    }

    private void generarCodigoPRecursivo(
            NodoExpresion nodo
    ) {

        if (nodo == null) {
            return;
        }

        if (nodo.esHoja()) {

            codigoP.add(
                    "LDC " + nodo.getValor()
            );

            return;
        }

        generarCodigoPRecursivo(
                nodo.getIzquierdo()
        );

        generarCodigoPRecursivo(
                nodo.getDerecho()
        );

        switch (nodo.getValor()) {

            case "+" ->
                    codigoP.add("ADD");

            case "-" ->
                    codigoP.add("SUB");

            case "*" ->
                    codigoP.add("MUL");

            case "/" ->
                    codigoP.add("DIV");

            default ->
                    codigoP.add(
                            "OP " + nodo.getValor()
                    );
        }
    }

    // =========================================================
    // TRIPLOS
    // =========================================================

    public String generarTriplos(
            NodoExpresion nodo
    ) {

        triplos.clear();

        if (nodo == null) {
            return "";
        }

        generarTriplosRecursivo(nodo);

        StringBuilder resultado =
                new StringBuilder();

        for (Triplo triplo : triplos) {

            resultado.append(
                    triplo
            );

            resultado.append("\n");
        }

        return resultado.toString();
    }

    private String generarTriplosRecursivo(
            NodoExpresion nodo
    ) {

        if (nodo.esHoja()) {
            return nodo.getValor();
        }

        String izquierdo =
                generarTriplosRecursivo(
                        nodo.getIzquierdo()
                );

        String derecho =
                generarTriplosRecursivo(
                        nodo.getDerecho()
                );

        int indice =
                triplos.size();

        Triplo triplo =
                new Triplo(
                        indice,
                        nodo.getValor(),
                        izquierdo,
                        derecho
                );

        triplos.add(triplo);

        return "(" + indice + ")";
    }

    // =========================================================
    // CUÁDRUPLOS
    // =========================================================

    public String generarCuadruplos(
            NodoExpresion nodo
    ) {

        cuadruplos.clear();
        contadorTemporales = 1;

        if (nodo == null) {
            return "";
        }

        generarCuadruplosRecursivo(nodo);

        StringBuilder resultado =
                new StringBuilder();

        for (Cuadruplo cuadruplo : cuadruplos) {

            resultado.append(
                    cuadruplo
            );

            resultado.append("\n");
        }

        return resultado.toString();
    }

    private String generarCuadruplosRecursivo(
            NodoExpresion nodo
    ) {

        if (nodo.esHoja()) {
            return nodo.getValor();
        }

        String izquierdo =
                generarCuadruplosRecursivo(
                        nodo.getIzquierdo()
                );

        String derecho =
                generarCuadruplosRecursivo(
                        nodo.getDerecho()
                );

        String temporal =
                nuevoTemporal();

        Cuadruplo cuadruplo =
                new Cuadruplo(
                        nodo.getValor(),
                        izquierdo,
                        derecho,
                        temporal
                );

        cuadruplos.add(cuadruplo);

        return temporal;
    }

    // =========================================================
    // TEMPORALES
    // =========================================================

    private String nuevoTemporal() {

        return "t"
                + contadorTemporales++;
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public List<String> getCodigoTresDirecciones() {

        return codigoTresDirecciones;
    }

    public List<String> getCodigoP() {

        return codigoP;
    }

    public List<Triplo> getTriplos() {

        return triplos;
    }

    public List<Cuadruplo> getCuadruplos() {

        return cuadruplos;
    }

    // =========================================================
    // CLASE TRIPLO
    // =========================================================

    public static class Triplo {

        private final int indice;
        private final String operador;
        private final String argumento1;
        private final String argumento2;

        public Triplo(
                int indice,
                String operador,
                String argumento1,
                String argumento2
        ) {

            this.indice = indice;
            this.operador = operador;
            this.argumento1 = argumento1;
            this.argumento2 = argumento2;
        }

        @Override
        public String toString() {

            return "("
                    + indice
                    + ") "
                    + operador
                    + " "
                    + argumento1
                    + " "
                    + argumento2;
        }
    }

    // =========================================================
    // CLASE CUÁDRUPLO
    // =========================================================

    public static class Cuadruplo {

        private final String operador;
        private final String argumento1;
        private final String argumento2;
        private final String resultado;

        public Cuadruplo(
                String operador,
                String argumento1,
                String argumento2,
                String resultado
        ) {

            this.operador = operador;
            this.argumento1 = argumento1;
            this.argumento2 = argumento2;
            this.resultado = resultado;
        }

        @Override
        public String toString() {

            return "("
                    + operador
                    + ", "
                    + argumento1
                    + ", "
                    + argumento2
                    + ", "
                    + resultado
                    + ")";
        }
    }
}