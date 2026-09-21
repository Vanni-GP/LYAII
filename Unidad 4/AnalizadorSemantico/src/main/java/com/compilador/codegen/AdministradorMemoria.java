package com.compilador.codegen;

import java.util.*;

public class AdministradorMemoria {

    private final Map<String, String> registros; // Registro -> Variable/Valor asignado
    private final Map<String, Integer> mapaOffsets; // Variable -> Offset en Data Segment
    private int offsetActual;
    private final List<String> bitacoraRegistros;

    public AdministradorMemoria() {
        registros = new LinkedHashMap<>();
        mapaOffsets = new LinkedHashMap<>();
        bitacoraRegistros = new ArrayList<>();
        inicializarRegistros();
    }

    private void inicializarRegistros() {
        registros.put("AX", "LIBRE");
        registros.put("BX", "LIBRE");
        registros.put("CX", "LIBRE");
        registros.put("DX", "LIBRE");
    }

    public void limpiar() {
        mapaOffsets.clear();
        bitacoraRegistros.clear();
        offsetActual = 0;
        inicializarRegistros();
    }

    public void registrarVariable(String nombre) {
        if (!mapaOffsets.containsKey(nombre)) {
            mapaOffsets.put(nombre, offsetActual);
            offsetActual += 2; // Formato Word de 16 bits (2 bytes por variable)
        }
    }

    public void registrarUsoRegistro(String instruccion, String detalle) {
        bitacoraRegistros.add("• [" + instruccion + "] " + detalle);
    }

    public void asignarRegistro(String reg, String contenido) {
        if (registros.containsKey(reg)) {
            registros.put(reg, contenido);
        }
    }

    public String getEstadoRegistros() {
        StringBuilder sb = new StringBuilder("=== 4.1 ASIGNACIÓN Y ESTADO DE REGISTROS ===\n\n");
        sb.append("BITÁCORA DE USO DE REGISTROS DE LA CPU:\n");
        for (String log : bitacoraRegistros) {
            sb.append("  ").append(log).append("\n");
        }

        sb.append("\nESTADO FINAL DE REGISTROS (CPU INTEL x86):\n");
        sb.append("------------------------------------------\n");
        for (Map.Entry<String, String> entry : registros.entrySet()) {
            sb.append(String.format("  %-4s : %s\n", entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }

    public String generarMapaMemoria(int tamanioCodigoBytes) {
        StringBuilder sb = new StringBuilder("================================================================================\n");
        sb.append("                    4.4 MAPA DE ADMINISTRACIÓN DE MEMORIA\n");
        sb.append("================================================================================\n\n");

        sb.append("1. SEGMENTO DE CÓDIGO (CODE SEGMENT - CS)\n");
        sb.append(String.format("   Base: 0x1000 | Tamaño Ocupado: %d bytes (0x%04X)\n", tamanioCodigoBytes, tamanioCodigoBytes));
        sb.append(String.format("   Rango: CS:0000h - CS:%04Xh\n", Math.max(0, tamanioCodigoBytes - 1)));
        sb.append("   Función: Aloja las instrucciones y códigos de operación ejecutables de la CPU.\n\n");

        sb.append("2. SEGMENTO DE DATOS (DATA SEGMENT - DS)\n");
        sb.append(String.format("   Base: 0x2000 | Rango: DS:0000h - DS:%04Xh\n", Math.max(0, offsetActual - 1)));
        sb.append("   -----------------------------------------------------------------------------\n");
        sb.append("   Variable       | Tipo     | Tamaño  | Offset Relativo | Dirección Física     \n");
        sb.append("   -----------------------------------------------------------------------------\n");

        for (Map.Entry<String, Integer> entry : mapaOffsets.entrySet()) {
            String variable = entry.getKey();
            int offset = entry.getValue();
            int dirFisica = 0x20000 + offset;
            sb.append(String.format("   %-14s | INT/WORD | 2 bytes | DS:%04Xh        | 0x%05X\n",
                    variable, offset, dirFisica));
        }

        sb.append("\n3. SEGMENTO DE PILA (STACK SEGMENT - SS)\n");
        sb.append("   Base: 0x3000 | Tamaño Asignado: 256 bytes (0x0100)\n");
        sb.append("   Rango: SS:0000h - SS:00FFh | Punteros: SP = 0x00FE, BP = 0x0000\n");
        sb.append("   Función: Soporte para resguardo de temporales (Spill) y llamadas a funciones.\n");

        return sb.toString();
    }

    public Map<String, Integer> getMapaOffsets() {
        return mapaOffsets;
    }
}