package com.compilador.optimization;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Optimizador {

    private final List<String> codigoOriginal;
    private List<String> codigoMirilla;
    private List<String> codigoLocal;
    private List<String> codigoCiclos;
    private List<String> codigoGlobal;
    private List<BloqueBasico> bloquesBasicos;

    // Bitácoras explicativas por tema
    private final List<String> bitacoraMirilla;
    private final List<String> bitacoraLocal;
    private final List<String> bitacoraCiclos;
    private final List<String> bitacoraGlobal;

    public Optimizador() {
        codigoOriginal = new ArrayList<>();
        codigoMirilla = new ArrayList<>();
        codigoLocal = new ArrayList<>();
        codigoCiclos = new ArrayList<>();
        codigoGlobal = new ArrayList<>();
        bloquesBasicos = new ArrayList<>();

        bitacoraMirilla = new ArrayList<>();
        bitacoraLocal = new ArrayList<>();
        bitacoraCiclos = new ArrayList<>();
        bitacoraGlobal = new ArrayList<>();
    }

    public void limpiar() {
        codigoOriginal.clear();
        codigoMirilla.clear();
        codigoLocal.clear();
        codigoCiclos.clear();
        codigoGlobal.clear();
        bloquesBasicos.clear();

        bitacoraMirilla.clear();
        bitacoraLocal.clear();
        bitacoraCiclos.clear();
        bitacoraGlobal.clear();
    }

    public void optimizar(List<String> instruccionesIntermedias) {
        limpiar();
        if (instruccionesIntermedias == null || instruccionesIntermedias.isEmpty()) {
            return;
        }

        codigoOriginal.addAll(instruccionesIntermedias);

        // 3.1.4 Mirilla
        ejecutarMirilla();

        // 3.1.1 Locales (Bloques básicos, propagación y subexpresiones)
        ejecutarLocal();

        // 3.1.2 Ciclos (Invariantes de bucle)
        ejecutarCiclos();

        // 3.1.3 Globales & 3.2.3 Flujo de datos (Código muerto)
        ejecutarGlobal();
    }

    // =========================================================
    // 3.1.4 OPTIMIZACIÓN DE MIRILLA (PEEPHOLE)
    // =========================================================

    private void ejecutarMirilla() {
        List<String> entrada = new ArrayList<>(codigoOriginal);
        boolean cambio = true;
        int iteracion = 1;

        while (cambio && iteracion <= 5) {
            cambio = false;
            List<String> salida = new ArrayList<>();

            for (int i = 0; i < entrada.size(); i++) {
                String actual = entrada.get(i).trim();

                // 1. Simplificación algebraica: x = y + 0 -> x = y
                if (actual.matches(".+\\s*=\\s*.+\\s*\\+\\s*0$")) {
                    String optimizada = actual.replaceAll("\\s*\\+\\s*0$", "");
                    bitacoraMirilla.add("• [Mirilla] Identidad aditiva eliminada: \"" + actual + "\" → \"" + optimizada + "\"");
                    salida.add(optimizada);
                    cambio = true;
                    continue;
                }

                // 2. Simplificación algebraica: x = y * 1 -> x = y
                if (actual.matches(".+\\s*=\\s*.+\\s*\\*\\s*1$")) {
                    String optimizada = actual.replaceAll("\\s*\\*\\s*1$", "");
                    bitacoraMirilla.add("• [Mirilla] Identidad multiplicativa eliminada: \"" + actual + "\" → \"" + optimizada + "\"");
                    salida.add(optimizada);
                    cambio = true;
                    continue;
                }

                // 3. Reducción de potencia: x = y * 2 -> x = y + y
                Pattern patP = Pattern.compile("^([a-zA-Z0-9_]+)\\s*=\\s*([a-zA-Z0-9_]+)\\s*\\*\\s*2$");
                Matcher matP = patP.matcher(actual);
                if (matP.matches()) {
                    String dest = matP.group(1);
                    String oper = matP.group(2);
                    String optimizada = dest + " = " + oper + " + " + oper;
                    bitacoraMirilla.add("• [Mirilla] Reducción de fuerza (*2 a suma): \"" + actual + "\" → \"" + optimizada + "\"");
                    salida.add(optimizada);
                    cambio = true;
                    continue;
                }

                // 4. Eliminación de saltos redundantes a la siguiente etiqueta
                if (actual.startsWith("GOTO ") && i + 1 < entrada.size()) {
                    String siguiente = entrada.get(i + 1).trim();
                    String etiquetaSalto = actual.substring(5).trim();
                    if (siguiente.equals(etiquetaSalto + ":")) {
                        bitacoraMirilla.add("• [Mirilla] Salto redundante descartado: \"" + actual + "\" precede inmediatamente a \"" + siguiente + "\"");
                        cambio = true;
                        continue;
                    }
                }

                // 5. Cargas / asignaciones inversas redundantes: a = b; b = a;
                if (i + 1 < entrada.size()) {
                    String sig = entrada.get(i + 1).trim();
                    if (actual.matches("^[a-zA-Z0-9_]+\\s*=\\s*[a-zA-Z0-9_]+$") &&
                            sig.matches("^[a-zA-Z0-9_]+\\s*=\\s*[a-zA-Z0-9_]+$")) {
                        String[] partes1 = actual.split("=");
                        String[] partes2 = sig.split("=");
                        String d1 = partes1[0].trim();
                        String o1 = partes1[1].trim();
                        String d2 = partes2[0].trim();
                        String o2 = partes2[1].trim();

                        if (d1.equals(o2) && o1.equals(d2)) {
                            salida.add(actual);
                            bitacoraMirilla.add("• [Mirilla] Copia reversa redundante eliminada: \"" + sig + "\"");
                            i++; // omitir la siguiente instrucción
                            cambio = true;
                            continue;
                        }
                    }
                }

                salida.add(actual);
            }
            entrada = salida;
            iteracion++;
        }

        codigoMirilla = entrada;
        if (bitacoraMirilla.isEmpty()) {
            bitacoraMirilla.add("No se detectaron patrones de mirilla aplicables.");
        }
    }

    // =========================================================
    // 3.1.1 OPTIMIZACIÓN LOCAL (BLOQUES BÁSICOS & PROPAGACIÓN)
    // =========================================================

    private void ejecutarLocal() {
        bloquesBasicos = segmentarEnBloquesBasicos(codigoMirilla);
        List<String> resultadoLocal = new ArrayList<>();

        for (BloqueBasico bloque : bloquesBasicos) {
            Map<String, String> constantes = new HashMap<>();
            Map<String, String> expresionesPrevias = new HashMap<>(); // "a + b" -> "t1"
            List<String> optimizadasBloque = new ArrayList<>();

            for (String linea : bloque.getInstrucciones()) {
                String inst = linea.trim();

                // Reemplazo de variables por constantes conocidas en este bloque
                for (Map.Entry<String, String> entry : constantes.entrySet()) {
                    String patron = "\\b" + entry.getKey() + "\\b";
                    if (inst.contains("=") && !inst.startsWith(entry.getKey() + " =")) {
                        String[] partes = inst.split("=", 2);
                        String nuevoLadoDer = partes[1].replaceAll(patron, entry.getValue());
                        if (!nuevoLadoDer.equals(partes[1])) {
                            bitacoraLocal.add("• [Local " + bloque.getId() + "] Propagación de constante: " + entry.getKey() + " = " + entry.getValue() + " en \"" + inst + "\"");
                            inst = partes[0] + "=" + nuevoLadoDer;
                        }
                    }
                }

                // Detección de asignación simple de constante: x = 5
                if (inst.matches("^[a-zA-Z0-9_]+\\s*=\\s*\\d+(\\.\\d+)?$")) {
                    String[] partes = inst.split("=");
                    constantes.put(partes[0].trim(), partes[1].trim());
                    optimizadasBloque.add(inst);
                    continue;
                }

                // Detección de subexpresiones comunes: t2 = a + b
                if (inst.matches("^[a-zA-Z0-9_]+\\s*=\\s*[a-zA-Z0-9_]+\\s*[+\\-*/]\\s*[a-zA-Z0-9_]+$")) {
                    String[] partes = inst.split("=");
                    String temporalDestino = partes[0].trim();
                    String operacion = partes[1].trim();

                    if (expresionesPrevias.containsKey(operacion)) {
                        String temporalReutilizado = expresionesPrevias.get(operacion);
                        String nuevaInst = temporalDestino + " = " + temporalReutilizado;
                        bitacoraLocal.add("• [Local " + bloque.getId() + "] Eliminación de subexpresión común: \"" + operacion + "\" ya calculada en " + temporalReutilizado);
                        optimizadasBloque.add(nuevaInst);
                        continue;
                    } else {
                        expresionesPrevias.put(operacion, temporalDestino);
                    }
                }

                optimizadasBloque.add(inst);
            }

            resultadoLocal.addAll(optimizadasBloque);
        }

        codigoLocal = resultadoLocal;
        if (bitacoraLocal.isEmpty()) {
            bitacoraLocal.add("No se detectaron subexpresiones comunes locales ni propagaciones dentro de los bloques.");
        }
    }

    private List<BloqueBasico> segmentarEnBloquesBasicos(List<String> codigo) {
        List<BloqueBasico> bloques = new ArrayList<>();
        Set<Integer> lideres = new TreeSet<>();
        lideres.add(0); // Regla 1: la primera instrucción es líder

        for (int i = 0; i < codigo.size(); i++) {
            String inst = codigo.get(i).trim();
            // Regla 2: destino de un salto es líder
            if (inst.endsWith(":")) {
                lideres.add(i);
            }
            // Regla 3: la instrucción inmediatamente después de un salto es líder
            if (inst.startsWith("GOTO ") || inst.startsWith("IF ") || inst.startsWith("IF_FALSE ")) {
                if (i + 1 < codigo.size()) {
                    lideres.add(i + 1);
                }
            }
        }

        List<Integer> listaLideres = new ArrayList<>(lideres);
        for (int i = 0; i < listaLideres.size(); i++) {
            int inicio = listaLideres.get(i);
            int fin = (i + 1 < listaLideres.size()) ? listaLideres.get(i + 1) : codigo.size();

            BloqueBasico b = new BloqueBasico("B" + (i + 1));
            for (int j = inicio; j < fin; j++) {
                b.agregarInstruccion(codigo.get(j));
            }
            bloques.add(b);
        }

        return bloques;
    }

    // =========================================================
    // 3.1.2 OPTIMIZACIÓN DE CICLOS (INVARIANTES & INDUCCIÓN)
    // =========================================================

    private void ejecutarCiclos() {
        List<String> entrada = new ArrayList<>(codigoLocal);
        List<String> salida = new ArrayList<>();
        boolean enCiclo = false;
        List<String> cuerpoCiclo = new ArrayList<>();
        List<String> invariantesDetectados = new ArrayList<>();

        for (int i = 0; i < entrada.size(); i++) {
            String inst = entrada.get(i).trim();

            if (inst.startsWith("WHILE ") || inst.startsWith("FOR ") || (inst.endsWith(":") && inst.startsWith("L"))) {
                enCiclo = true;
                salida.add(inst);
                continue;
            }

            if (enCiclo && (inst.startsWith("IF_FALSE ") || inst.startsWith("GOTO "))) {
                // Fin de encabezado o salto de fin de bucle
                for (String cInst : cuerpoCiclo) {
                    // Si una operación dentro del bucle asigna una expresión constante o cálculo fijo:
                    if (cInst.matches("^[a-zA-Z0-9_]+\\s*=\\s*\\d+\\s*[+\\-*/]\\s*\\d+$")) {
                        invariantesDetectados.add(cInst);
                        bitacoraCiclos.add("• [Ciclos] Código invariante extraído fuera del bucle (Code Motion): \"" + cInst + "\"");
                    } else {
                        salida.add(cInst);
                    }
                }
                // Los invariantes se mueven al pre-encabezado
                for (int inv = 0; inv < invariantesDetectados.size(); inv++) {
                    salida.add(salida.size() - 1, invariantesDetectados.get(inv));
                }
                cuerpoCiclo.clear();
                invariantesDetectados.clear();
                enCiclo = false;
            }

            if (enCiclo) {
                cuerpoCiclo.add(inst);
            } else {
                salida.add(inst);
            }
        }

        if (!cuerpoCiclo.isEmpty()) {
            salida.addAll(cuerpoCiclo);
        }

        codigoCiclos = salida;
        if (bitacoraCiclos.isEmpty()) {
            bitacoraCiclos.add("No se encontraron cálculos invariantes dentro de ciclos para extracción.");
        }
    }

    // =========================================================
    // 3.1.3 OPTIMIZACIÓN GLOBAL & 3.2.3 FLUJO DE DATOS
    // =========================================================

    private void ejecutarGlobal() {
        List<String> entrada = new ArrayList<>(codigoCiclos);
        Set<String> variablesUsadas = new HashSet<>();

        // 1. Análisis de flujo de datos: recolección de variables leídas / vivas
        for (String inst : entrada) {
            String linea = inst.trim();
            if (linea.contains("=")) {
                String[] partes = linea.split("=", 2);
                String ladoDerecho = partes[1].trim();
                Matcher m = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b").matcher(ladoDerecho);
                while (m.find()) {
                    variablesUsadas.add(m.group());
                }
            } else if (linea.startsWith("IF ") || linea.startsWith("IF_FALSE ") || linea.startsWith("PARAM ") || linea.startsWith("return ")) {
                Matcher m = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b").matcher(linea);
                while (m.find()) {
                    String tok = m.group();
                    if (!tok.equals("IF") && !tok.equals("IF_FALSE") && !tok.equals("GOTO") && !tok.equals("PARAM") && !tok.equals("return")) {
                        variablesUsadas.add(tok);
                    }
                }
            }
        }

        // 2. Eliminación de código muerto (Dead Code Elimination)
        List<String> salida = new ArrayList<>();
        for (String inst : entrada) {
            String linea = inst.trim();

            if (linea.matches("^[a-zA-Z0-9_]+\\s*=.*$") && !linea.startsWith("return")) {
                String[] partes = linea.split("=", 2);
                String varDestino = partes[0].trim();

                // Si es un temporal generado que nunca se lee en adelante, es código muerto
                if (varDestino.startsWith("t") && !variablesUsadas.contains(varDestino)) {
                    bitacoraGlobal.add("• [Global - Dead Code] Asignación inútil descartada: \"" + linea + "\" (" + varDestino + " no vuelve a ser leído)");
                    continue;
                }
            }
            salida.add(inst);
        }

        codigoGlobal = salida;
        if (bitacoraGlobal.isEmpty()) {
            bitacoraGlobal.add("Análisis de datos finalizado: no se encontraron asignaciones a variables muertas.");
        }
    }

    // =========================================================
    // 3.2 COSTOS Y CRITERIOS
    // =========================================================

    public Map<String, Integer> calcularMetricas(List<String> codigo) {
        int totalInstrucciones = codigo.size();
        int accesosMemoria = 0;
        int operacionesAritmeticas = 0;
        int saltos = 0;

        for (String inst : codigo) {
            String linea = inst.trim();
            if (linea.contains("+") || linea.contains("-") || linea.contains("*") || linea.contains("/")) {
                operacionesAritmeticas++;
            }
            if (linea.startsWith("GOTO ") || linea.startsWith("IF ") || linea.startsWith("IF_FALSE ")) {
                saltos++;
            }
            if (linea.contains("=") || linea.startsWith("PARAM ") || linea.startsWith("DECL ")) {
                accesosMemoria += 2; // Carga y almacenamiento promedio
            }
        }

        Map<String, Integer> metricas = new LinkedHashMap<>();
        metricas.put("Instrucciones Totales", totalInstrucciones);
        metricas.put("Accesos Estimados a Memoria", accesosMemoria);
        metricas.put("Operaciones Aritméticas", operacionesAritmeticas);
        metricas.put("Saltos y Bifurcaciones", saltos);
        return metricas;
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public List<String> getCodigoOriginal() { return codigoOriginal; }
    public List<String> getCodigoMirilla() { return codigoMirilla; }
    public List<String> getCodigoLocal() { return codigoLocal; }
    public List<String> getCodigoCiclos() { return codigoCiclos; }
    public List<String> getCodigoGlobal() { return codigoGlobal; }
    public List<BloqueBasico> getBloquesBasicos() { return bloquesBasicos; }

    public List<String> getBitacoraMirilla() { return bitacoraMirilla; }
    public List<String> getBitacoraLocal() { return bitacoraLocal; }
    public List<String> getBitacoraCiclos() { return bitacoraCiclos; }
    public List<String> getBitacoraGlobal() { return bitacoraGlobal; }
}