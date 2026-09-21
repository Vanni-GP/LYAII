package com.compilador.codegen;

import java.util.*;

public class MapeadorMaquina {

    private int totalBytes;

    public MapeadorMaquina() {
        this.totalBytes = 0;
    }

    public String generarLenguajeMaquina(List<String> lineasAsm, Map<String, Integer> offsets) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s | %-16s | %s\n", "OFFSET CS", "CÓDIGO MÁQUINA", "INSTRUCCIÓN ASM"));
        sb.append("------------------------------------------------------------------\n");

        int pc = 0;
        boolean enSegmentoCodigo = false;

        for (String linea : lineasAsm) {
            String trimmed = linea.trim();

            // Activar el mapeo únicamente a partir de la directiva .CODE
            if (trimmed.equals(".CODE")) {
                enSegmentoCodigo = true;
                continue;
            }

            // Ignorar encabezados y las declaraciones de variables en .DATA
            if (!enSegmentoCodigo) {
                continue;
            }

            if (trimmed.isEmpty() || trimmed.startsWith(";") || trimmed.startsWith(".")
                    || trimmed.endsWith("PROC") || trimmed.endsWith("ENDP") || trimmed.endsWith("MAIN")) {
                continue;
            }

            String bytesHex = codificarLinea(trimmed, offsets);
            int tamBytes = (bytesHex.replace(" ", "").length()) / 2;

            sb.append(String.format("CS:%04X    | %-16s | %s\n", pc, bytesHex, trimmed));
            pc += tamBytes;
        }

        this.totalBytes = pc;
        return sb.toString();
    }

    private String codificarLinea(String inst, Map<String, Integer> offsets) {
        // MOV AX, imm (ej: MOV AX, 10)
        if (inst.matches("^MOV\\s+AX,\\s*\\d+$")) {
            int val = Integer.parseInt(inst.replaceAll("[^0-9]", ""));
            return String.format("B8 %02X %02X", val & 0xFF, (val >> 8) & 0xFF);
        }

        // MOV AX, @DATA
        if (inst.equals("MOV AX, @DATA")) {
            return "B8 00 20";
        }

        // MOV DS, AX
        if (inst.equals("MOV DS, AX")) {
            return "8E D8";
        }

        // MOV [var], AX
        if (inst.matches("^MOV\\s+[a-zA-Z_][a-zA-Z0-9_]*,\\s*AX$")) {
            String var = inst.substring(4, inst.indexOf(",")).trim();
            int off = offsets.getOrDefault(var, 0);
            return String.format("A3 %02X %02X", off & 0xFF, (off >> 8) & 0xFF);
        }

        // MOV AX, [var]
        if (inst.matches("^MOV\\s+AX,\\s*[a-zA-Z_][a-zA-Z0-9_]*$")) {
            String var = inst.substring(inst.indexOf(",") + 1).trim();
            int off = offsets.getOrDefault(var, 0);
            return String.format("A1 %02X %02X", off & 0xFF, (off >> 8) & 0xFF);
        }

        // ADD AX, [var] o ADD AX, imm
        if (inst.startsWith("ADD AX,")) {
            String op = inst.substring(7).trim();
            if (op.matches("\\d+")) {
                int val = Integer.parseInt(op);
                return String.format("05 %02X %02X", val & 0xFF, (val >> 8) & 0xFF);
            } else {
                int off = offsets.getOrDefault(op, 0);
                return String.format("03 06 %02X %02X", off & 0xFF, (off >> 8) & 0xFF);
            }
        }

        // SUB AX, [var]
        if (inst.startsWith("SUB AX,")) {
            String op = inst.substring(7).trim();
            int off = offsets.getOrDefault(op, 0);
            return String.format("2B 06 %02X %02X", off & 0xFF, (off >> 8) & 0xFF);
        }

        // IMUL BX
        if (inst.equals("IMUL BX")) return "F7 EB";

        // IDIV BX
        if (inst.equals("IDIV BX")) return "F7 FB";

        // CWD
        if (inst.equals("CWD")) return "99";

        // Interrupción de salida
        if (inst.equals("MOV AH, 4Ch")) return "B4 4C";
        if (inst.equals("INT 21h")) return "CD 21";

        return "90"; // NOP genérico para casos no contemplados
    }

    public int getTotalBytes() {
        return totalBytes;
    }
}