package com.compilador.codegen;

import java.util.*;

public class GeneradorEnsamblador {

    private final AdministradorMemoria memoria;
    private final List<String> instruccionesAsm;
    private final Set<String> variablesDeclaradas;

    public GeneradorEnsamblador(AdministradorMemoria memoria) {
        this.memoria = memoria;
        this.instruccionesAsm = new ArrayList<>();
        this.variablesDeclaradas = new LinkedHashSet<>();
    }

    public void limpiar() {
        instruccionesAsm.clear();
        variablesDeclaradas.clear();
    }

    public List<String> traducir(List<String> codigoTresDirecciones) {
        limpiar();

        for (String linea : codigoTresDirecciones) {
            String inst = linea.trim();
            if (inst.isEmpty() || inst.startsWith("//")) continue;

            if (inst.contains("=")) {
                String[] partes = inst.split("=", 2);
                String dest = partes[0].trim();
                String expr = partes[1].trim();

                variablesDeclaradas.add(dest);
                memoria.registrarVariable(dest);

                procesarAsignacion(dest, expr, inst);
            }
        }
        return construirProgramaCompleto();
    }

    private void procesarAsignacion(String dest, String expr, String instOriginal) {
        // Expresión binaria: dest = op1 + op2 (o -, *, /)
        if (expr.matches("^[a-zA-Z0-9_]+\\s*[+\\-*/]\\s*[a-zA-Z0-9_]+$")) {
            String[] tokens = expr.split("\\s+");
            String op1 = tokens[0];
            String operador = tokens[1];
            String op2 = tokens[2];

            if (op1.matches("[a-zA-Z_][a-zA-Z0-9_]*")) variablesDeclaradas.add(op1);
            if (op2.matches("[a-zA-Z_][a-zA-Z0-9_]*")) variablesDeclaradas.add(op2);

            instruccionesAsm.add("    ; " + instOriginal);
            instruccionesAsm.add("    MOV AX, " + op1);
            memoria.asignarRegistro("AX", op1);
            memoria.registrarUsoRegistro(instOriginal, "Carga " + op1 + " en AX");

            switch (operador) {
                case "+":
                    instruccionesAsm.add("    ADD AX, " + op2);
                    memoria.registrarUsoRegistro(instOriginal, "Suma " + op2 + " al registro AX");
                    break;
                case "-":
                    instruccionesAsm.add("    SUB AX, " + op2);
                    memoria.registrarUsoRegistro(instOriginal, "Resta " + op2 + " de AX");
                    break;
                case "*":
                    instruccionesAsm.add("    MOV BX, " + op2);
                    instruccionesAsm.add("    IMUL BX");
                    memoria.asignarRegistro("BX", op2);
                    memoria.registrarUsoRegistro(instOriginal, "Multiplica AX por BX (guarda resultado en AX)");
                    break;
                case "/":
                    instruccionesAsm.add("    MOV BX, " + op2);
                    instruccionesAsm.add("    CWD");
                    instruccionesAsm.add("    IDIV BX");
                    memoria.asignarRegistro("BX", op2);
                    memoria.registrarUsoRegistro(instOriginal, "Divide AX entre BX");
                    break;
            }

            instruccionesAsm.add("    MOV " + dest + ", AX");
            memoria.asignarRegistro("AX", dest + " (Resultado)");
            memoria.registrarUsoRegistro(instOriginal, "Guarda el acumulador AX en [" + dest + "]");
            instruccionesAsm.add("");
            return;
        }

        // Asignación simple: dest = valor / identificador
        instruccionesAsm.add("    ; " + instOriginal);
        instruccionesAsm.add("    MOV AX, " + expr);
        instruccionesAsm.add("    MOV " + dest + ", AX");
        instruccionesAsm.add("");

        memoria.asignarRegistro("AX", expr);
        memoria.registrarUsoRegistro(instOriginal, "Mueve " + expr + " a AX y lo almacena en [" + dest + "]");
    }

    private List<String> construirProgramaCompleto() {
        List<String> asm = new ArrayList<>();
        asm.add("; =========================================================");
        asm.add("; CÓDIGO ENSAMBLADOR (x86 - 16 BITS / INTEL)");
        asm.add("; Lenguajes y Autómatas II — Unidad 4");
        asm.add("; =========================================================");
        asm.add(".MODEL SMALL");
        asm.add(".STACK 100h");
        asm.add("");
        asm.add(".DATA");

        for (String var : variablesDeclaradas) {
            asm.add(String.format("    %-10s DW 0", var));
        }

        asm.add("");
        asm.add(".CODE");
        asm.add("MAIN PROC");
        asm.add("    ; Inicializar puntero al segmento de datos");
        asm.add("    MOV AX, @DATA");
        asm.add("    MOV DS, AX");
        asm.add("");

        asm.addAll(instruccionesAsm);

        asm.add("    ; Retorno limpio al sistema operativo");
        asm.add("    MOV AH, 4Ch");
        asm.add("    INT 21h");
        asm.add("MAIN ENDP");
        asm.add("END MAIN");

        return asm;
    }

    public List<String> getInstruccionesAsm() {
        return instruccionesAsm;
    }
}