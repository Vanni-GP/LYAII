package com.compilador.intermediate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generador de código intermedio para la Unidad 2.
 *
 * Implementa:
 *
 * 2.3.1 Variables y constantes
 * 2.3.2 Expresiones
 * 2.3.3 Instrucciones de asignación
 * 2.3.4 Instrucciones de control
 * 2.3.5 Funciones
 * 2.3.6 Estructuras
 *
 * También genera:
 *
 * - Código de tres direcciones
 * - Código P
 * - Triplos
 * - Cuádruplos
 */
public class GeneradorEsquema {

    private int temporal;
    private int etiqueta;
    private int indiceTriplo;

    private final List<String> tresDirecciones;
    private final List<String> codigoP;
    private final List<String> triplos;
    private final List<String> cuadruplos;

    private final List<String> variables;
    private final List<String> asignaciones;
    private final List<String> controles;
    private final List<String> funciones;
    private final List<String> estructuras;

    public GeneradorEsquema() {

        tresDirecciones = new ArrayList<>();
        codigoP = new ArrayList<>();
        triplos = new ArrayList<>();
        cuadruplos = new ArrayList<>();

        variables = new ArrayList<>();
        asignaciones = new ArrayList<>();
        controles = new ArrayList<>();
        funciones = new ArrayList<>();
        estructuras = new ArrayList<>();

        limpiar();
    }

    // =========================================================
    // LIMPIAR
    // =========================================================

    public void limpiar() {

        temporal = 1;
        etiqueta = 1;
        indiceTriplo = 0;

        tresDirecciones.clear();
        codigoP.clear();
        triplos.clear();
        cuadruplos.clear();

        variables.clear();
        asignaciones.clear();
        controles.clear();
        funciones.clear();
        estructuras.clear();
    }

    // =========================================================
    // MÉTODO PRINCIPAL
    // =========================================================

    public void generar(String codigo) {

        limpiar();

        if (codigo == null || codigo.isBlank()) {
            return;
        }

        String[] lineas =
                codigo.split("\\R");

        boolean dentroFuncion = false;
        int nivelLlaves = 0;

        for (int i = 0; i < lineas.length; i++) {

            String linea =
                    lineas[i].trim();

            if (linea.isEmpty()) {
                continue;
            }

            // Comentarios
            if (linea.startsWith("//")) {
                continue;
            }

            // -------------------------------------------------
            // FUNCIONES
            // -------------------------------------------------

            if (esFuncion(linea)) {

                dentroFuncion = true;

                procesarFuncion(linea);

                if (linea.contains("{")) {
                    nivelLlaves++;
                }

                continue;
            }

            // -------------------------------------------------
            // LLAVES
            // -------------------------------------------------

            if (linea.equals("}")) {

                if (nivelLlaves > 0) {
                    nivelLlaves--;
                }

                if (nivelLlaves == 0) {
                    dentroFuncion = false;
                }

                continue;
            }

            // -------------------------------------------------
            // ESTRUCTURAS / ARREGLOS
            // -------------------------------------------------

            if (esEstructura(linea)) {

                procesarEstructura(linea);

                continue;
            }

            // -------------------------------------------------
            // CONTROL
            // -------------------------------------------------

            if (esControl(linea)) {

                procesarControl(linea);

                continue;
            }

            // -------------------------------------------------
            // VARIABLES / CONSTANTES
            // -------------------------------------------------

            if (esVariable(linea)) {

                procesarVariable(linea);

                continue;
            }

            // -------------------------------------------------
            // ASIGNACIÓN
            // -------------------------------------------------

            if (esAsignacion(linea)) {

                procesarAsignacion(linea);

                continue;
            }

            // -------------------------------------------------
            // RETURN
            // -------------------------------------------------

            if (linea.startsWith("return ")) {

                String expresion =
                        quitarPuntoComa(
                                linea.substring(7)
                        ).trim();

                String resultado =
                        generarExpresion(
                                expresion
                        );

                tresDirecciones.add(
                        "return " + resultado
                );

                codigoP.add(
                        "RET " + resultado
                );

                continue;
            }

            // -------------------------------------------------
            // LLAMADA A FUNCIÓN
            // -------------------------------------------------

            if (esLlamadaFuncion(linea)) {

                procesarLlamadaFuncion(linea);

                continue;
            }
        }
    }

    // =========================================================
    // 2.3.1 VARIABLES Y CONSTANTES
    // =========================================================

    private boolean esVariable(String linea) {

        return linea.matches(
                "^(final\\s+)?"
                        + "(int|float|double|boolean|String)"
                        + "\\s+"
                        + "[a-zA-Z_][a-zA-Z0-9_]*"
                        + "(\\s*=.*)?;"
        );
    }

    private void procesarVariable(String linea) {

        String original =
                linea.trim();

        boolean constante =
                original.startsWith("final ");

        String texto =
                original.replaceFirst(
                        "^final\\s+",
                        ""
                );

        Pattern patron =
                Pattern.compile(
                        "^(int|float|double|boolean|String)"
                                + "\\s+"
                                + "([a-zA-Z_][a-zA-Z0-9_]*"
                                + ")"
                                + "\\s*(?:=\\s*(.+))?;"
                );

        Matcher matcher =
                patron.matcher(texto);

        if (!matcher.matches()) {
            return;
        }

        String tipo =
                matcher.group(1);

        String nombre =
                matcher.group(2);

        String expresion =
                matcher.group(3);

        String registro =
                (constante
                        ? "constante "
                        : "variable ")
                        + nombre
                        + " : "
                        + tipo;

        variables.add(registro);

        if (expresion == null) {

            tresDirecciones.add(
                    "DECL " + nombre
            );

            codigoP.add(
                    "DECL " + tipo
                            + " " + nombre
            );

            return;
        }

        expresion =
                expresion.trim();

        String resultado =
                generarExpresion(
                        expresion
                );

        tresDirecciones.add(
                nombre
                        + " = "
                        + resultado
        );

        codigoP.add(
                "STO " + nombre
        );
    }

    // =========================================================
    // 2.3.2 EXPRESIONES
    // =========================================================

    private String generarExpresion(
            String expresion
    ) {

        if (expresion == null
                || expresion.isBlank()) {

            return "";
        }

        expresion =
                quitarPuntoComa(
                        expresion
                ).trim();

        // ---------------------------------------------
        // Operación
        // ---------------------------------------------

        ParserExpresion parser =
                new ParserExpresion(
                        expresion
                );

        Nodo nodo =
                parser.parse();

        return generarNodo(
                nodo
        );
    }

    // =========================================================
    // GENERACIÓN DEL ÁRBOL INTERMEDIO
    // =========================================================

    private String generarNodo(
            Nodo nodo
    ) {

        if (nodo == null) {
            return "";
        }

        if (nodo.esHoja()) {

            codigoP.add(
                    "LDC " + nodo.valor
            );

            return nodo.valor;
        }

        String izquierda =
                generarNodo(
                        nodo.izquierdo
                );

        String derecha =
                generarNodo(
                        nodo.derecho
                );

        String temp =
                nuevoTemporal();

        // -------------------------------------------------
        // TRES DIRECCIONES
        // -------------------------------------------------

        tresDirecciones.add(
                temp
                        + " = "
                        + izquierda
                        + " "
                        + nodo.valor
                        + " "
                        + derecha
        );

        // -------------------------------------------------
        // CÓDIGO P
        // -------------------------------------------------

        switch (nodo.valor) {

            case "+" ->
                    codigoP.add("ADD");

            case "-" ->
                    codigoP.add("SUB");

            case "*" ->
                    codigoP.add("MUL");

            case "/" ->
                    codigoP.add("DIV");

            case ">" ->
                    codigoP.add("GT");

            case "<" ->
                    codigoP.add("LT");

            case ">=" ->
                    codigoP.add("GE");

            case "<=" ->
                    codigoP.add("LE");

            case "==" ->
                    codigoP.add("EQ");

            case "!=" ->
                    codigoP.add("NE");

            default ->
                    codigoP.add(
                            "OP " + nodo.valor
                    );
        }

        // -------------------------------------------------
        // TRIPLO
        // -------------------------------------------------

        triplos.add(
                "("
                        + indiceTriplo
                        + ") "
                        + nodo.valor
                        + " "
                        + izquierda
                        + " "
                        + derecha
        );

        indiceTriplo++;

        // -------------------------------------------------
        // CUÁDRUPLO
        // -------------------------------------------------

        cuadruplos.add(
                "("
                        + nodo.valor
                        + ", "
                        + izquierda
                        + ", "
                        + derecha
                        + ", "
                        + temp
                        + ")"
        );

        return temp;
    }

    // =========================================================
    // 2.3.3 ASIGNACIONES
    // =========================================================

    private boolean esAsignacion(
            String linea
    ) {

        return linea.matches(
                "^[a-zA-Z_][a-zA-Z0-9_]*"
                        + "\\s*=.*;"
        );
    }

    private void procesarAsignacion(
            String linea
    ) {

        Pattern patron =
                Pattern.compile(
                        "^([a-zA-Z_][a-zA-Z0-9_]*)"
                                + "\\s*=\\s*(.+);"
                );

        Matcher matcher =
                patron.matcher(linea);

        if (!matcher.matches()) {
            return;
        }

        String destino =
                matcher.group(1);

        String expresion =
                matcher.group(2).trim();

        String resultado =
                generarExpresion(
                        expresion
                );

        asignaciones.add(
                destino
                        + " = "
                        + expresion
        );

        tresDirecciones.add(
                destino
                        + " = "
                        + resultado
        );

        codigoP.add(
                "STO " + destino
        );
    }

    // =========================================================
    // 2.3.4 CONTROL
    // =========================================================

    private boolean esControl(
            String linea
    ) {

        return linea.startsWith("if ")
                || linea.startsWith("if(")
                || linea.startsWith("while ")
                || linea.startsWith("while(")
                || linea.startsWith("for ")
                || linea.startsWith("for(")
                || linea.equals("else")
                || linea.startsWith("else ");
    }

    private void procesarControl(
            String linea
    ) {

        // -------------------------------------------------
        // IF
        // -------------------------------------------------

        if (linea.startsWith("if")) {

            String condicion =
                    extraerCondicion(
                            linea
                    );

            String resultado =
                    generarExpresion(
                            condicion
                    );

            String verdadera =
                    nuevaEtiqueta();

            String falsa =
                    nuevaEtiqueta();

            controles.add(
                    "IF "
                            + condicion
            );

            tresDirecciones.add(
                    "IF "
                            + resultado
                            + " GOTO "
                            + verdadera
            );

            tresDirecciones.add(
                    "GOTO "
                            + falsa
            );

            tresDirecciones.add(
                    verdadera + ":"
            );

            codigoP.add(
                    "JPC "
                            + verdadera
            );

            codigoP.add(
                    "JMP "
                            + falsa
            );

            return;
        }

        // -------------------------------------------------
        // ELSE
        // -------------------------------------------------

        if (linea.startsWith("else")) {

            String etiquetaElse =
                    nuevaEtiqueta();

            controles.add(
                    "ELSE"
            );

            tresDirecciones.add(
                    etiquetaElse + ":"
            );

            codigoP.add(
                    etiquetaElse + ":"
            );

            return;
        }

        // -------------------------------------------------
        // WHILE
        // -------------------------------------------------

        if (linea.startsWith("while")) {

            String condicion =
                    extraerCondicion(
                            linea
                    );

            String inicio =
                    nuevaEtiqueta();

            String salida =
                    nuevaEtiqueta();

            controles.add(
                    "WHILE "
                            + condicion
            );

            tresDirecciones.add(
                    inicio + ":"
            );

            String resultado =
                    generarExpresion(
                            condicion
                    );

            tresDirecciones.add(
                    "IF_FALSE "
                            + resultado
                            + " GOTO "
                            + salida
            );

            codigoP.add(
                    "LABEL "
                            + inicio
            );

            codigoP.add(
                    "JPC "
                            + salida
            );

            return;
        }

        // -------------------------------------------------
        // FOR
        // -------------------------------------------------

        if (linea.startsWith("for")) {

            String contenido =
                    extraerCondicion(
                            linea
                    );

            controles.add(
                    "FOR "
                            + contenido
            );

            String inicio =
                    nuevaEtiqueta();

            String salida =
                    nuevaEtiqueta();

            tresDirecciones.add(
                    inicio + ":"
            );

            tresDirecciones.add(
                    "FOR "
                            + contenido
            );

            tresDirecciones.add(
                    "IF_FALSE GOTO "
                            + salida
            );

            codigoP.add(
                    "LABEL "
                            + inicio
            );

            codigoP.add(
                    "FOR "
                            + contenido
            );

            return;
        }
    }

    // =========================================================
    // 2.3.5 FUNCIONES
    // =========================================================

    private boolean esFuncion(
            String linea
    ) {

        return linea.matches(
                "^(public\\s+|private\\s+|protected\\s+)?"
                        + "(static\\s+)?"
                        + "(void|int|float|double|boolean|String)"
                        + "\\s+"
                        + "[a-zA-Z_][a-zA-Z0-9_]*"
                        + "\\s*\\(.*\\)\\s*\\{?"
        );
    }

    private void procesarFuncion(
            String linea
    ) {

        Pattern patron =
                Pattern.compile(
                        "^(?:public\\s+|private\\s+|protected\\s+)?"
                                + "(?:static\\s+)?"
                                + "(void|int|float|double|boolean|String)"
                                + "\\s+"
                                + "([a-zA-Z_][a-zA-Z0-9_]*)"
                                + "\\s*\\((.*?)\\)"
                );

        Matcher matcher =
                patron.matcher(
                        linea
                );

        if (!matcher.find()) {
            return;
        }

        String retorno =
                matcher.group(1);

        String nombre =
                matcher.group(2);

        String parametros =
                matcher.group(3);

        String funcion =
                nombre
                        + "("
                        + parametros
                        + ") : "
                        + retorno;

        funciones.add(
                funcion
        );

        tresDirecciones.add(
                "FUNC "
                        + nombre
        );

        codigoP.add(
                "PROC "
                        + nombre
        );
    }

    // =========================================================
    // LLAMADAS A FUNCIONES
    // =========================================================

    private boolean esLlamadaFuncion(
            String linea
    ) {

        return linea.matches(
                "^[a-zA-Z_][a-zA-Z0-9_]*"
                        + "\\s*\\(.*\\)\\s*;?"
        );
    }

    private void procesarLlamadaFuncion(
            String linea
    ) {

        String llamada =
                quitarPuntoComa(
                        linea
                );

        Pattern patron =
                Pattern.compile(
                        "^([a-zA-Z_][a-zA-Z0-9_]*)"
                                + "\\s*\\((.*)\\)$"
                );

        Matcher matcher =
                patron.matcher(
                        llamada
                );

        if (!matcher.matches()) {
            return;
        }

        String nombre =
                matcher.group(1);

        String parametros =
                matcher.group(2);

        String[] args =
                parametros.isBlank()
                        ? new String[0]
                        : parametros.split(",");

        for (String argumento : args) {

            String valor =
                    argumento.trim();

            tresDirecciones.add(
                    "PARAM "
                            + valor
            );

            codigoP.add(
                    "PAR "
                            + valor
            );
        }

        String temp =
                nuevoTemporal();

        tresDirecciones.add(
                temp
                        + " = CALL "
                        + nombre
                        + ", "
                        + args.length
        );

        codigoP.add(
                "CALL "
                        + nombre
                        + ", "
                        + args.length
        );
    }

    // =========================================================
    // 2.3.6 ESTRUCTURAS
    // =========================================================
    //
    // En esta implementación se manejan arreglos como
    // estructura de datos.
    // =========================================================

    private boolean esEstructura(
            String linea
    ) {

        return linea.matches(
                "^(int|float|double|String|boolean)"
                        + "\\s+"
                        + "[a-zA-Z_][a-zA-Z0-9_]*"
                        + "\\s*\\[.*\\].*;"
        );
    }

    private void procesarEstructura(
            String linea
    ) {

        Pattern patron =
                Pattern.compile(
                        "^(int|float|double|String|boolean)"
                                + "\\s+"
                                + "([a-zA-Z_][a-zA-Z0-9_]*)"
                                + "\\s*\\[(.*?)\\]"
                                + "\\s*(?:=\\s*(.*))?;"
                );

        Matcher matcher =
                patron.matcher(
                        linea
                );

        if (!matcher.matches()) {
            return;
        }

        String tipo =
                matcher.group(1);

        String nombre =
                matcher.group(2);

        String tamanio =
                matcher.group(3);

        String inicializacion =
                matcher.group(4);

        String registro =
                "arreglo "
                        + nombre
                        + " : "
                        + tipo
                        + "["
                        + tamanio
                        + "]";

        estructuras.add(
                registro
        );

        tresDirecciones.add(
                "ALLOC "
                        + nombre
                        + "["
                        + tamanio
                        + "]"
        );

        codigoP.add(
                "ALLOC "
                        + tipo
                        + " "
                        + nombre
                        + "["
                        + tamanio
                        + "]"
        );

        if (inicializacion != null) {

            tresDirecciones.add(
                    nombre
                            + " = "
                            + inicializacion
            );
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private String nuevoTemporal() {

        return "t"
                + temporal++;
    }

    private String nuevaEtiqueta() {

        return "L"
                + etiqueta++;
    }

    private String quitarPuntoComa(
            String texto
    ) {

        texto =
                texto.trim();

        if (texto.endsWith(";")) {

            return texto.substring(
                    0,
                    texto.length() - 1
            );
        }

        return texto;
    }

    private String extraerCondicion(
            String linea
    ) {

        int inicio =
                linea.indexOf("(");

        int fin =
                linea.lastIndexOf(")");

        if (inicio >= 0
                && fin > inicio) {

            return linea.substring(
                    inicio + 1,
                    fin
            ).trim();
        }

        return linea
                .replaceFirst(
                        "^(if|while|for)\\s*",
                        ""
                )
                .replace("{", "")
                .trim();
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public List<String> getTresDirecciones() {

        return tresDirecciones;
    }

    public List<String> getCodigoP() {

        return codigoP;
    }

    public List<String> getTriplos() {

        return triplos;
    }

    public List<String> getCuadruplos() {

        return cuadruplos;
    }

    public List<String> getVariables() {

        return variables;
    }

    public List<String> getAsignaciones() {

        return asignaciones;
    }

    public List<String> getControles() {

        return controles;
    }

    public List<String> getFunciones() {

        return funciones;
    }

    public List<String> getEstructuras() {

        return estructuras;
    }

    // =========================================================
    // NODO
    // =========================================================

    private static class Nodo {

        String valor;

        Nodo izquierdo;

        Nodo derecho;

        Nodo(String valor) {

            this.valor = valor;
        }

        Nodo(
                String valor,
                Nodo izquierdo,
                Nodo derecho
        ) {

            this.valor = valor;

            this.izquierdo =
                    izquierdo;

            this.derecho =
                    derecho;
        }

        boolean esHoja() {

            return izquierdo == null
                    && derecho == null;
        }
    }

    // =========================================================
    // PARSER DE EXPRESIONES
    // =========================================================

    private static class ParserExpresion {

        private final List<String> tokens;

        private int posicion;

        ParserExpresion(
                String expresion
        ) {

            tokens =
                    tokenizar(
                            expresion
                    );

            posicion = 0;
        }

        Nodo parse() {

            if (tokens.isEmpty()) {
                return null;
            }

            Nodo resultado =
                    expresion();

            return resultado;
        }

        private Nodo expresion() {

            Nodo nodo =
                    termino();

            while (
                    hay("+")
                            || hay("-")
            ) {

                String operador =
                        consumir();

                Nodo derecho =
                        termino();

                nodo =
                        new Nodo(
                                operador,
                                nodo,
                                derecho
                        );
            }

            return nodo;
        }

        private Nodo termino() {

            Nodo nodo =
                    factor();

            while (
                    hay("*")
                            || hay("/")
            ) {

                String operador =
                        consumir();

                Nodo derecho =
                        factor();

                nodo =
                        new Nodo(
                                operador,
                                nodo,
                                derecho
                        );
            }

            return nodo;
        }

        private Nodo factor() {

            if (hay("(")) {

                consumir();

                Nodo nodo =
                        expresion();

                if (hay(")")) {
                    consumir();
                }

                return nodo;
            }

            if (posicion >= tokens.size()) {
                return null;
            }

            return new Nodo(
                    consumir()
            );
        }

        private boolean hay(
                String token
        ) {

            return posicion
                    < tokens.size()
                    && tokens
                    .get(posicion)
                    .equals(token);
        }

        private String consumir() {

            return tokens.get(
                    posicion++
            );
        }

        private static List<String> tokenizar(
                String expresion
        ) {

            List<String> resultado =
                    new ArrayList<>();

            Pattern patron =
                    Pattern.compile(
                            "\\d+(?:\\.\\d+)?"
                                    + "|[a-zA-Z_][a-zA-Z0-9_]*"
                                    + "|[+\\-*/()]"
                    );

            Matcher matcher =
                    patron.matcher(
                            expresion
                    );

            while (matcher.find()) {

                resultado.add(
                        matcher.group()
                );
            }

            return resultado;
        }
    }
}