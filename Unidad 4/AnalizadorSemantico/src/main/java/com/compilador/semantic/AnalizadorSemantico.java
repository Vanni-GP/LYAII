package com.compilador.semantic;

import com.compilador.model.ErrorSemantico;
import com.compilador.model.NodoExpresion;
import com.compilador.model.Simbolo;
import com.compilador.model.TipoDato;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorSemantico {

    private final Map<String, Simbolo> tablaSimbolos;
    private final List<ErrorSemantico> errores;
    private final Deque<TipoDato> pilaSemantica;

    private final List<String> accionesSemanticas;
    private final List<String> traduccionIntermedia;

    private NodoExpresion ultimoArbol;

    private int siguienteDireccion;
    private int temporal;

    public AnalizadorSemantico() {

        tablaSimbolos = new LinkedHashMap<>();
        errores = new ArrayList<>();
        pilaSemantica = new ArrayDeque<>();

        accionesSemanticas = new ArrayList<>();
        traduccionIntermedia = new ArrayList<>();

        siguienteDireccion = 1000;
        temporal = 1;
    }

    public void analizar(String codigo) {

        tablaSimbolos.clear();
        errores.clear();
        pilaSemantica.clear();
        accionesSemanticas.clear();
        traduccionIntermedia.clear();

        ultimoArbol = null;

        siguienteDireccion = 1000;
        temporal = 1;

        String[] lineas = codigo.split("\\R");

        for (int i = 0; i < lineas.length; i++) {

            String linea = lineas[i].trim();

            if (linea.isEmpty()) {
                continue;
            }

            analizarLinea(linea, i + 1);
        }
    }

    private void analizarLinea(
            String linea,
            int numeroLinea
    ) {

        if (!linea.endsWith(";")) {

            agregarError(
                    numeroLinea,
                    "SEM-001",
                    "Falta ';' al final de la instrucción."
            );

            return;
        }

        linea = linea.substring(
                0,
                linea.length() - 1
        ).trim();

        // ==========================================
        // DECLARACIÓN
        // ==========================================

        Pattern declaracion = Pattern.compile(
                "^(int|float|double|boolean|String)\\s+"
                        + "([a-zA-Z_][a-zA-Z0-9_]*)"
                        + "\\s*(?:=\\s*(.+))?$"
        );

        Matcher md = declaracion.matcher(linea);

        if (md.matches()) {

            String tipoTexto = md.group(1);
            String nombre = md.group(2);
            String expresion = md.group(3);

            TipoDato tipo =
                    TipoDato.desdeTexto(tipoTexto);

            if (tablaSimbolos.containsKey(nombre)) {

                agregarError(
                        numeroLinea,
                        "SEM-002",
                        "La variable '" + nombre
                                + "' ya fue declarada."
                );

                return;
            }

            String valor = "";
            boolean inicializada = false;

            if (expresion != null) {

                expresion = expresion.trim();

                accionesSemanticas.add(
                        "Línea " + numeroLinea
                                + ": Verificando declaración de "
                                + nombre
                );

                TipoDato tipoExpresion =
                        analizarExpresion(
                                expresion,
                                numeroLinea
                        );

                if (!tiposCompatibles(
                        tipo,
                        tipoExpresion
                )) {

                    agregarError(
                            numeroLinea,
                            "SEM-003",
                            "No se puede asignar un valor de tipo "
                                    + tipoExpresion
                                    + " a una variable de tipo "
                                    + tipo
                                    + "."
                    );

                } else {

                    valor = expresion;
                    inicializada = true;

                    accionesSemanticas.add(
                            "Asignación válida: "
                                    + nombre
                                    + " ← "
                                    + expresion
                    );

                    generarTraduccion(
                            nombre,
                            expresion
                    );
                }
            }

            Simbolo simbolo = new Simbolo(
                    nombre,
                    tipo,
                    valor,
                    siguienteDireccion,
                    numeroLinea,
                    inicializada
            );

            tablaSimbolos.put(
                    nombre,
                    simbolo
            );

            accionesSemanticas.add(
                    "Agregando símbolo: "
                            + nombre
                            + " : "
                            + tipo
                            + " @"
                            + siguienteDireccion
            );

            siguienteDireccion += 4;

            return;
        }

        // ==========================================
        // ASIGNACIÓN
        // ==========================================

        Pattern asignacion = Pattern.compile(
                "^([a-zA-Z_][a-zA-Z0-9_]*)"
                        + "\\s*=\\s*(.+)$"
        );

        Matcher ma = asignacion.matcher(linea);

        if (ma.matches()) {

            String nombre = ma.group(1);
            String expresion = ma.group(2).trim();

            if (!tablaSimbolos.containsKey(nombre)) {

                agregarError(
                        numeroLinea,
                        "SEM-004",
                        "La variable '"
                                + nombre
                                + "' no ha sido declarada."
                );

                return;
            }

            Simbolo simbolo =
                    tablaSimbolos.get(nombre);

            TipoDato tipoExpresion =
                    analizarExpresion(
                            expresion,
                            numeroLinea
                    );

            if (!tiposCompatibles(
                    simbolo.getTipo(),
                    tipoExpresion
            )) {

                agregarError(
                        numeroLinea,
                        "SEM-003",
                        "No se puede asignar un valor de tipo "
                                + tipoExpresion
                                + " a una variable de tipo "
                                + simbolo.getTipo()
                                + "."
                );

            } else {

                simbolo.setValor(expresion);
                simbolo.setInicializada(true);

                accionesSemanticas.add(
                        "Asignación válida: "
                                + nombre
                                + " ← "
                                + expresion
                );

                generarTraduccion(
                        nombre,
                        expresion
                );
            }

            return;
        }

        agregarError(
                numeroLinea,
                "SEM-005",
                "Instrucción no reconocida."
        );
    }

    // ==================================================
    // ANÁLISIS DE EXPRESIONES
    // ==================================================

    private TipoDato analizarExpresion(
            String expresion,
            int linea
    ) {

        ultimoArbol = null;

        try {

            ParserExpresion parser =
                    new ParserExpresion(
                            expresion,
                            linea
                    );

            ultimoArbol =
                    parser.parse();

            if (ultimoArbol == null) {

                return TipoDato.DESCONOCIDO;
            }

            TipoDato tipo =
                    evaluarArbol(
                            ultimoArbol,
                            linea
                    );

            return tipo;

        } catch (Exception e) {

            agregarError(
                    linea,
                    "SEM-006",
                    "Expresión inválida: "
                            + expresion
            );

            return TipoDato.DESCONOCIDO;
        }
    }

    // ==================================================
    // EVALUACIÓN SEMÁNTICA DEL ÁRBOL
    // ==================================================

    private TipoDato evaluarArbol(
            NodoExpresion nodo,
            int linea
    ) {

        if (nodo == null) {

            return TipoDato.DESCONOCIDO;
        }

        // Hoja
        if (nodo.esHoja()) {

            TipoDato tipo = nodo.getTipo();

            pilaSemantica.push(tipo);

            accionesSemanticas.add(
                    "PUSH(" + tipo + ")"
            );

            return tipo;
        }

        TipoDato izquierdo =
                evaluarArbol(
                        nodo.getIzquierdo(),
                        linea
                );

        TipoDato derecho =
                evaluarArbol(
                        nodo.getDerecho(),
                        linea
                );

        String operador =
                nodo.getValor();

        if (operador.equals("+")
                && izquierdo == TipoDato.STRING
                && derecho == TipoDato.STRING) {

            pilaSemantica.pop();
            pilaSemantica.pop();

            pilaSemantica.push(
                    TipoDato.STRING
            );

            accionesSemanticas.add(
                    "POP(), POP(), PUSH(String)"
            );

            return TipoDato.STRING;
        }

        if (operador.equals("+")
                && izquierdo == TipoDato.STRING) {

            agregarError(
                    linea,
                    "SEM-007",
                    "No se puede realizar la operación "
                            + "String + "
                            + derecho
            );

            return TipoDato.DESCONOCIDO;
        }

        if (!izquierdo.esNumerico()
                || !derecho.esNumerico()) {

            agregarError(
                    linea,
                    "SEM-007",
                    "Operación '" + operador
                            + "' incompatible entre "
                            + izquierdo
                            + " y "
                            + derecho
            );

            return TipoDato.DESCONOCIDO;
        }

        if (operador.equals("/")
                && nodo.getDerecho().esHoja()
                && nodo.getDerecho().getValor().equals("0")) {

            agregarError(
                    linea,
                    "SEM-008",
                    "División entre cero."
            );

            return TipoDato.DESCONOCIDO;
        }

        TipoDato resultado =
                tipoNumericoResultado(
                        izquierdo,
                        derecho
                );

        nodo.setTipo(resultado);

        if (pilaSemantica.size() >= 2) {

            pilaSemantica.pop();
            pilaSemantica.pop();
        }

        pilaSemantica.push(resultado);

        accionesSemanticas.add(
                "Aplicar operador "
                        + operador
                        + ": "
                        + izquierdo
                        + " "
                        + operador
                        + " "
                        + derecho
                        + " → "
                        + resultado
        );

        return resultado;
    }

    private TipoDato tipoNumericoResultado(
            TipoDato a,
            TipoDato b
    ) {

        if (a == TipoDato.DOUBLE
                || b == TipoDato.DOUBLE) {

            return TipoDato.DOUBLE;
        }

        if (a == TipoDato.FLOAT
                || b == TipoDato.FLOAT) {

            return TipoDato.FLOAT;
        }

        return TipoDato.INT;
    }

    // ==================================================
    // COMPATIBILIDAD DE TIPOS
    // ==================================================

    private boolean tiposCompatibles(
            TipoDato destino,
            TipoDato origen
    ) {

        if (origen == TipoDato.DESCONOCIDO) {
            return false;
        }

        if (destino == origen) {
            return true;
        }

        if (destino == TipoDato.FLOAT
                && origen == TipoDato.INT) {

            return true;
        }

        if (destino == TipoDato.DOUBLE
                && origen.esNumerico()) {

            return true;
        }

        return false;
    }

    // ==================================================
    // CÓDIGO DE TRES DIRECCIONES
    // ==================================================

    private void generarTraduccion(
            String destino,
            String expresion
    ) {

        if (!contieneOperador(expresion)) {

            traduccionIntermedia.add(
                    destino
                            + " = "
                            + expresion
            );

            return;
        }

        try {

            ParserExpresion parser =
                    new ParserExpresion(
                            expresion,
                            0
                    );

            NodoExpresion arbol =
                    parser.parse();

            String resultado =
                    generarTresDirecciones(arbol);

            traduccionIntermedia.add(
                    destino
                            + " = "
                            + resultado
            );

        } catch (Exception ignored) {
        }
    }

    private String generarTresDirecciones(
            NodoExpresion nodo
    ) {

        if (nodo.esHoja()) {

            return nodo.getValor();
        }

        String izquierda =
                generarTresDirecciones(
                        nodo.getIzquierdo()
                );

        String derecha =
                generarTresDirecciones(
                        nodo.getDerecho()
                );

        String temp =
                "t" + temporal++;

        traduccionIntermedia.add(
                temp
                        + " = "
                        + izquierda
                        + " "
                        + nodo.getValor()
                        + " "
                        + derecha
        );

        return temp;
    }

    private boolean contieneOperador(
            String expresion
    ) {

        return expresion.contains("+")
                || expresion.contains("-")
                || expresion.contains("*")
                || expresion.contains("/");
    }

    // ==================================================
    // ERRORES
    // ==================================================

    private void agregarError(
            int linea,
            String codigo,
            String mensaje
    ) {

        errores.add(
                new ErrorSemantico(
                        linea,
                        codigo,
                        mensaje
                )
        );
    }

    // ==================================================
    // GETTERS
    // ==================================================

    public Map<String, Simbolo> getTablaSimbolos() {

        return tablaSimbolos;
    }

    public List<ErrorSemantico> getErrores() {

        return errores;
    }

    public Deque<TipoDato> getPilaSemantica() {

        return pilaSemantica;
    }

    public List<String> getAccionesSemanticas() {

        return accionesSemanticas;
    }

    public List<String> getTraduccionIntermedia() {

        return traduccionIntermedia;
    }

    public NodoExpresion getUltimoArbol() {

        return ultimoArbol;
    }

    public boolean hayErrores() {

        return !errores.isEmpty();
    }

    // ==================================================
    // PARSER DE EXPRESIONES
    // ==================================================

    private class ParserExpresion {

        private final List<Token> tokens;
        private int posicion;
        private final int linea;

        ParserExpresion(
                String expresion,
                int linea
        ) {

            this.linea = linea;
            tokens = tokenizar(expresion);
            posicion = 0;
        }

        NodoExpresion parse() {

            NodoExpresion resultado =
                    expresion();

            if (posicion < tokens.size()) {

                throw new IllegalArgumentException();
            }

            return resultado;
        }

        private NodoExpresion expresion() {

            NodoExpresion nodo =
                    termino();

            while (hay("+")
                    || hay("-")) {

                String operador =
                        consumir().texto;

                NodoExpresion derecho =
                        termino();

                nodo = new NodoExpresion(
                        operador,
                        TipoDato.DESCONOCIDO,
                        nodo,
                        derecho
                );
            }

            return nodo;
        }

        private NodoExpresion termino() {

            NodoExpresion nodo =
                    factor();

            while (hay("*")
                    || hay("/")) {

                String operador =
                        consumir().texto;

                NodoExpresion derecho =
                        factor();

                nodo = new NodoExpresion(
                        operador,
                        TipoDato.DESCONOCIDO,
                        nodo,
                        derecho
                );
            }

            return nodo;
        }

        private NodoExpresion factor() {

            if (hay("(")) {

                consumir();

                NodoExpresion nodo =
                        expresion();

                if (!hay(")")) {

                    throw new IllegalArgumentException();
                }

                consumir();

                return nodo;
            }

            if (posicion >= tokens.size()) {

                throw new IllegalArgumentException();
            }

            Token token =
                    consumir();

            if (token.tipo.equals("NUM_INT")) {

                return new NodoExpresion(
                        token.texto,
                        TipoDato.INT
                );
            }

            if (token.tipo.equals("NUM_FLOAT")) {

                return new NodoExpresion(
                        token.texto,
                        TipoDato.FLOAT
                );
            }

            if (token.tipo.equals("NUM_DOUBLE")) {

                return new NodoExpresion(
                        token.texto,
                        TipoDato.DOUBLE
                );
            }

            if (token.tipo.equals("STRING")) {

                return new NodoExpresion(
                        token.texto,
                        TipoDato.STRING
                );
            }

            if (token.tipo.equals("BOOLEAN")) {

                return new NodoExpresion(
                        token.texto,
                        TipoDato.BOOLEAN
                );
            }

            if (token.tipo.equals("ID")) {

                if (!tablaSimbolos.containsKey(
                        token.texto
                )) {

                    agregarError(
                            linea,
                            "SEM-004",
                            "La variable '"
                                    + token.texto
                                    + "' no ha sido declarada."
                    );

                    return new NodoExpresion(
                            token.texto,
                            TipoDato.DESCONOCIDO
                    );
                }

                Simbolo simbolo =
                        tablaSimbolos.get(
                                token.texto
                        );

                if (!simbolo.isInicializada()) {

                    agregarError(
                            linea,
                            "SEM-009",
                            "La variable '"
                                    + token.texto
                                    + "' no ha sido inicializada."
                    );
                }

                return new NodoExpresion(
                        token.texto,
                        simbolo.getTipo()
                );
            }

            throw new IllegalArgumentException();
        }

        private boolean hay(String texto) {

            return posicion < tokens.size()
                    && tokens.get(posicion)
                    .texto
                    .equals(texto);
        }

        private Token consumir() {

            return tokens.get(
                    posicion++
            );
        }
    }

    // ==================================================
    // TOKENIZADOR DE EXPRESIONES
    // ==================================================

    private List<Token> tokenizar(
            String expresion
    ) {

        List<Token> resultado =
                new ArrayList<>();

        Pattern patron = Pattern.compile(
                "\"[^\"]*\""
                        + "|\\d+\\.\\d+f"
                        + "|\\d+\\.\\d+"
                        + "|\\d+"
                        + "|true|false"
                        + "|[a-zA-Z_][a-zA-Z0-9_]*"
                        + "|[+\\-*/()]"
        );

        Matcher matcher =
                patron.matcher(expresion);

        int ultimo = 0;

        while (matcher.find()) {

            String espacio =
                    expresion.substring(
                            ultimo,
                            matcher.start()
                    );

            if (!espacio.trim().isEmpty()) {

                throw new IllegalArgumentException();
            }

            String texto =
                    matcher.group();

            String tipo;

            if (texto.matches("\\d+")) {

                tipo = "NUM_INT";

            } else if (texto.matches(
                    "\\d+\\.\\d+f"
            )) {

                tipo = "NUM_FLOAT";

            } else if (texto.matches(
                    "\\d+\\.\\d+"
            )) {

                tipo = "NUM_DOUBLE";

            } else if (texto.startsWith("\"")) {

                tipo = "STRING";

            } else if (
                    texto.equals("true")
                            || texto.equals("false")
            ) {

                tipo = "BOOLEAN";

            } else if (
                    texto.matches(
                            "[a-zA-Z_][a-zA-Z0-9_]*"
                    )
            ) {

                tipo = "ID";

            } else {

                tipo = "OPERADOR";
            }

            resultado.add(
                    new Token(
                            texto,
                            tipo
                    )
            );

            ultimo = matcher.end();
        }

        if (!expresion
                .substring(ultimo)
                .trim()
                .isEmpty()) {

            throw new IllegalArgumentException();
        }

        return resultado;
    }

    private record Token(
            String texto,
            String tipo
    ) {
    }
}