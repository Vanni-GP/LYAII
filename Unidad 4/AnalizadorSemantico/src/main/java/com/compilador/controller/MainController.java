package com.compilador.controller;

import com.compilador.codegen.AdministradorMemoria;
import com.compilador.codegen.GeneradorEnsamblador;
import com.compilador.codegen.MapeadorMaquina;
import com.compilador.intermediate.GeneradorCodigoIntermedio;
import com.compilador.intermediate.GeneradorEsquema;
import com.compilador.model.ErrorSemantico;
import com.compilador.model.NodoExpresion;
import com.compilador.model.Simbolo;
import com.compilador.optimization.BloqueBasico;
import com.compilador.optimization.Optimizador;
import com.compilador.semantic.AnalizadorSemantico;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class MainController {

    private final AnalizadorSemantico analizador;
    private final GeneradorCodigoIntermedio generador;
    private final GeneradorEsquema generadorEsquema;
    private final Optimizador optimizador;

    // Unidad 4: Código Objeto
    private final AdministradorMemoria memoria;
    private final GeneradorEnsamblador generadorAsm;
    private final MapeadorMaquina mapeadorMaquina;

    // Editor y Consola
    private TextArea codigoArea;
    private TextArea consolaRapidaArea;

    // Unidad 1 — Semántica
    private TextArea resultadoArea;
    private TextArea accionesArea;
    private TextArea pilaArea;
    private TextArea traduccionArea;

    // Unidad 2.1 y 2.2 — Notaciones y Representaciones
    private TextArea infijaArea;
    private TextArea prefijaArea;
    private TextArea postfijaArea;
    private TextArea polacaArea;
    private TextArea codigoPArea;
    private TextArea triplosArea;
    private TextArea cuadruplosArea;

    // Unidad 2.3 — Esquemas de Generación
    private TextArea variablesArea;
    private TextArea asignacionesArea;
    private TextArea controlesArea;
    private TextArea funcionesArea;
    private TextArea estructurasArea;
    private TextArea generacion23Area;

    // Unidad 3 — Optimización
    private TextArea mirillaArea;
    private TextArea bloquesBasicosArea;
    private TextArea localArea;
    private TextArea ciclosArea;
    private TextArea globalArea;
    private TextArea costosArea;

    // Unidad 4 — Código Objeto
    private TextArea registrosArea;
    private TextArea ensambladorArea;
    private TextArea maquinaArea;
    private TextArea memoriaArea;

    // Estructuras de Datos
    private TableView<Simbolo> tabla;
    private TreeView<String> arbol;

    public MainController() {
        analizador = new AnalizadorSemantico();
        generador = new GeneradorCodigoIntermedio();
        generadorEsquema = new GeneradorEsquema();
        optimizador = new Optimizador();

        memoria = new AdministradorMemoria();
        generadorAsm = new GeneradorEnsamblador(memoria);
        mapeadorMaquina = new MapeadorMaquina();
    }

    public void mostrar(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e2e;");

        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(crearEncabezado(), crearBarraHerramientas());
        root.setTop(topContainer);

        SplitPane splitPrincipal = new SplitPane();
        splitPrincipal.setOrientation(Orientation.HORIZONTAL);
        splitPrincipal.setStyle("-fx-box-border: transparent; -fx-background-color: transparent;");

        VBox panelIzquierdo = crearPanelIzquierdo();
        VBox panelDerecho = crearPanelDerecho();

        splitPrincipal.getItems().addAll(panelIzquierdo, panelDerecho);
        splitPrincipal.setDividerPositions(0.38);

        root.setCenter(splitPrincipal);
        BorderPane.setMargin(splitPrincipal, new Insets(10));

        Scene scene = new Scene(root, 1400, 860);
        aplicarEstilosGlobales(scene);

        stage.setTitle("Entorno de Desarrollo — Compilador LyA II");
        stage.setScene(scene);
        stage.show();
    }

    private VBox crearEncabezado() {
        Label titulo = new Label("COMPILADOR — LENGUAJES Y AUTÓMATAS II");
        titulo.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label subtitulo = new Label("U1: Semántica  |  U2: Código Intermedio  |  U3: Optimización  |  U4: Código Objeto");
        subtitulo.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 12px;");

        VBox encabezado = new VBox(3, titulo, subtitulo);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        encabezado.setPadding(new Insets(12, 18, 6, 18));
        encabezado.setStyle("-fx-background-color: #181825; -fx-border-color: #313244; -fx-border-width: 0 0 1 0;");
        return encabezado;
    }

    private ToolBar crearBarraHerramientas() {
        Button btnAnalizar = new Button("▶ Compilar Completo (U1 a U4)");
        btnAnalizar.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAnalizar.setOnAction(e -> analizar());

        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #45475a; -fx-text-fill: #cdd6f4; -fx-cursor: hand;");
        btnLimpiar.setOnAction(e -> limpiar());

        Button btnEjemploU4 = new Button("Ejemplo U4");
        btnEjemploU4.setStyle("-fx-background-color: #f9e2af; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-cursor: hand;");
        btnEjemploU4.setOnAction(e -> cargarEjemploUnidad4());

        Button btnEjemploOptim = new Button("Ejemplo U3");
        btnEjemploOptim.setStyle("-fx-background-color: #a6e3a1; -fx-text-fill: #11111b; -fx-cursor: hand;");
        btnEjemploOptim.setOnAction(e -> cargarEjemploOptimizacion());

        ToolBar toolBar = new ToolBar(
                btnAnalizar,
                new Separator(Orientation.VERTICAL),
                btnLimpiar,
                new Separator(Orientation.VERTICAL),
                btnEjemploU4,
                btnEjemploOptim
        );
        toolBar.setStyle("-fx-background-color: #1e1e2e; -fx-border-color: #313244; -fx-border-width: 0 0 1 0; -fx-padding: 8 16;");
        return toolBar;
    }

    private VBox crearPanelIzquierdo() {
        Label lblEditor = new Label("Editor de Código Fuente");
        lblEditor.setStyle("-fx-text-fill: #89b4fa; -fx-font-weight: bold; -fx-font-size: 13px;");

        codigoArea = new TextArea();
        codigoArea.setPromptText("// Escribe tu código aquí...");
        codigoArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 14px; -fx-control-inner-background: #11111b; -fx-text-fill: #cdd6f4;");

        Label lblConsola = new Label("Consola de Estado / Compilación");
        lblConsola.setStyle("-fx-text-fill: #a6adc8; -fx-font-weight: bold; -fx-font-size: 12px;");

        consolaRapidaArea = new TextArea();
        consolaRapidaArea.setEditable(false);
        consolaRapidaArea.setPrefHeight(160);
        consolaRapidaArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-control-inner-background: #11111b; -fx-text-fill: #f38ba8;");

        VBox editorBox = new VBox(6, lblEditor, codigoArea);
        VBox.setVgrow(codigoArea, Priority.ALWAYS);

        VBox consolaBox = new VBox(6, lblConsola, consolaRapidaArea);

        SplitPane splitIzquierdo = new SplitPane(editorBox, consolaBox);
        splitIzquierdo.setOrientation(Orientation.VERTICAL);
        splitIzquierdo.setDividerPositions(0.75);
        splitIzquierdo.setStyle("-fx-box-border: transparent; -fx-background-color: transparent;");

        VBox contenedor = new VBox(splitIzquierdo);
        contenedor.setPadding(new Insets(6));
        VBox.setVgrow(splitIzquierdo, Priority.ALWAYS);
        return contenedor;
    }

    private VBox crearPanelDerecho() {
        TabPane tabPrincipal = new TabPane();
        tabPrincipal.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabU1 = new Tab("1. Semántica", construirPanelUnidad1());
        Tab tabU2 = new Tab("2. Código Intermedio", construirPanelUnidad2());
        Tab tabU3 = new Tab("3. Optimización", construirPanelUnidad3());
        Tab tabU4 = new Tab("4. Código Objeto", construirPanelUnidad4());
        Tab tabDatos = new Tab("Estructuras & Símbolos", construirPanelEstructuras());

        tabPrincipal.getTabs().addAll(tabU1, tabU2, tabU3, tabU4, tabDatos);

        VBox contenedor = new VBox(tabPrincipal);
        contenedor.setPadding(new Insets(6));
        VBox.setVgrow(tabPrincipal, Priority.ALWAYS);
        return contenedor;
    }

    private TabPane construirPanelUnidad1() {
        TabPane subTabs = new TabPane();
        subTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        resultadoArea = crearAreaSalida();
        accionesArea = crearAreaSalida();
        pilaArea = crearAreaSalida();
        traduccionArea = crearAreaSalida();

        subTabs.getTabs().addAll(
                new Tab("Resultado", crearContenedorTab("Diagnóstico Semántico", resultadoArea)),
                new Tab("Acciones", crearContenedorTab("Acciones Semánticas del Analizador", accionesArea)),
                new Tab("Pila Semántica", crearContenedorTab("Registro de Estados de la Pila", pilaArea)),
                new Tab("Traducción Inicial", crearContenedorTab("Traducción Intermedia U1", traduccionArea))
        );
        return subTabs;
    }

    private TabPane construirPanelUnidad2() {
        TabPane subTabs = new TabPane();
        subTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        infijaArea = crearAreaSalida();
        prefijaArea = crearAreaSalida();
        postfijaArea = crearAreaSalida();
        polacaArea = crearAreaSalida();
        codigoPArea = crearAreaSalida();
        triplosArea = crearAreaSalida();
        cuadruplosArea = crearAreaSalida();

        variablesArea = crearAreaSalida();
        asignacionesArea = crearAreaSalida();
        controlesArea = crearAreaSalida();
        funcionesArea = crearAreaSalida();
        estructurasArea = crearAreaSalida();
        generacion23Area = crearAreaSalida();

        subTabs.getTabs().addAll(
                new Tab("Cuádruplos", crearContenedorTab("Representación en Cuádruplos", cuadruplosArea)),
                new Tab("Triplos", crearContenedorTab("Representación en Triplos", triplosArea)),
                new Tab("Código P", crearContenedorTab("Código para Máquina de Pila (Código P)", codigoPArea)),
                new Tab("Notaciones", construirSubPanelNotaciones()),
                new Tab("Esquema Completo 2.3", crearContenedorTab("Desglose de Esquemas de Traducción", generacion23Area)),
                new Tab("Componentes 2.3", construirSubPanelComponentes())
        );
        return subTabs;
    }

    private TabPane construirSubPanelNotaciones() {
        TabPane notacionesTabs = new TabPane();
        notacionesTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        notacionesTabs.getTabs().addAll(
                new Tab("Infija", crearContenedorTab("Notación Infija", infijaArea)),
                new Tab("Prefija", crearContenedorTab("Notación Prefija", prefijaArea)),
                new Tab("Postfija", crearContenedorTab("Notación Postfija", postfijaArea)),
                new Tab("Polaca", crearContenedorTab("Notación Polaca", polacaArea))
        );
        return notacionesTabs;
    }

    private TabPane construirSubPanelComponentes() {
        TabPane compTabs = new TabPane();
        compTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        compTabs.getTabs().addAll(
                new Tab("Variables", crearContenedorTab("2.3.1 Declaraciones", variablesArea)),
                new Tab("Asignaciones", crearContenedorTab("2.3.3 Asignaciones", asignacionesArea)),
                new Tab("Control", crearContenedorTab("2.3.4 Control de Flujo", controlesArea)),
                new Tab("Funciones", crearContenedorTab("2.3.5 Procedimientos / Funciones", funcionesArea)),
                new Tab("Estructuras", crearContenedorTab("2.3.6 Arreglos y Estructuras", estructurasArea))
        );
        return compTabs;
    }

    private TabPane construirPanelUnidad3() {
        TabPane subTabs = new TabPane();
        subTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        mirillaArea = crearAreaSalida();
        bloquesBasicosArea = crearAreaSalida();
        localArea = crearAreaSalida();
        ciclosArea = crearAreaSalida();
        globalArea = crearAreaSalida();
        costosArea = crearAreaSalida();

        subTabs.getTabs().addAll(
                new Tab("3.1.4 Mirilla", crearContenedorTab("Optimización de Mirilla (Peephole)", mirillaArea)),
                new Tab("Bloques Básicos", crearContenedorTab("Segmentación del Grafo de Flujo", bloquesBasicosArea)),
                new Tab("3.1.1 Locales", crearContenedorTab("Subexpresiones Comunes y Propagación Local", localArea)),
                new Tab("3.1.2 Ciclos", crearContenedorTab("Código Invariante y Reducción en Bucles", ciclosArea)),
                new Tab("3.1.3 Globales", crearContenedorTab("Análisis de Flujo y Código Muerto", globalArea)),
                new Tab("3.2 Costos y Criterios", crearContenedorTab("Evaluación Comparativa de Rendimiento", costosArea))
        );
        return subTabs;
    }

    // =========================================================
    // PANEL UNIDAD 4: CÓDIGO OBJETO
    // =========================================================

    private TabPane construirPanelUnidad4() {
        TabPane subTabs = new TabPane();
        subTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        registrosArea = crearAreaSalida();
        ensambladorArea = crearAreaSalida();
        maquinaArea = crearAreaSalida();
        memoriaArea = crearAreaSalida();

        subTabs.getTabs().addAll(
                new Tab("4.1 Registros", crearContenedorTab("Asignación y Estado de Registros de CPU", registrosArea)),
                new Tab("4.2 Ensamblador", crearContenedorTab("Programa en Ensamblador x86 (.ASM)", ensambladorArea)),
                new Tab("4.3 Lenguaje Máquina", crearContenedorTab("Opcodes y Volcado en Hexadecimal", maquinaArea)),
                new Tab("4.4 Memoria", crearContenedorTab("Mapa de Segmentos y Direccionamiento", memoriaArea))
        );
        return subTabs;
    }

    private SplitPane construirPanelEstructuras() {
        tabla = new TableView<>();
        TableColumn<Simbolo, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));

        TableColumn<Simbolo, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().toString()));

        TableColumn<Simbolo, String> colValor = new TableColumn<>("Valor");
        colValor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getValor()));

        TableColumn<Simbolo, String> colDir = new TableColumn<>("Dirección");
        colDir.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getDireccion())));

        TableColumn<Simbolo, String> colLinea = new TableColumn<>("Línea");
        colLinea.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getLinea())));

        TableColumn<Simbolo, String> colInit = new TableColumn<>("Inicializada");
        colInit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isInicializada() ? "Sí" : "No"));

        tabla.getColumns().addAll(colNombre, colTipo, colValor, colDir, colLinea, colInit);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox panelTabla = crearContenedorTab("1.6 Tabla de Símbolos y Direcciones", tabla);

        arbol = new TreeView<>();
        arbol.setShowRoot(true);
        VBox panelArbol = crearContenedorTab("1.1 Árbol de Expresión", arbol);

        SplitPane split = new SplitPane(panelTabla, panelArbol);
        split.setOrientation(Orientation.VERTICAL);
        split.setDividerPositions(0.55);
        return split;
    }

    // =========================================================
    // FLUJO COMPLETO DE COMPILACIÓN
    // =========================================================

    private void analizar() {
        String codigo = codigoArea.getText();

        if (codigo.trim().isEmpty()) {
            consolaRapidaArea.setStyle("-fx-font-family: 'Consolas'; -fx-control-inner-background: #11111b; -fx-text-fill: #fab387;");
            consolaRapidaArea.setText("Advertencia: No se ha ingresado código fuente.");
            return;
        }

        // 1. Análisis Semántico
        analizador.analizar(codigo);

        tabla.getItems().clear();
        tabla.getItems().addAll(analizador.getTablaSimbolos().values());

        StringBuilder resultado = new StringBuilder();
        if (analizador.hayErrores()) {
            consolaRapidaArea.setStyle("-fx-font-family: 'Consolas'; -fx-control-inner-background: #11111b; -fx-text-fill: #f38ba8;");
            StringBuilder consola = new StringBuilder("✗ Falló la compilación semántica:\n");

            resultado.append("✗ ERRORES SEMÁNTICOS ENCONTRADOS:\n\n");
            for (ErrorSemantico error : analizador.getErrores()) {
                resultado.append("• ").append(error).append("\n");
                consola.append("  ").append(error).append("\n");
            }
            consolaRapidaArea.setText(consola.toString());
            return;
        } else {
            consolaRapidaArea.setStyle("-fx-font-family: 'Consolas'; -fx-control-inner-background: #11111b; -fx-text-fill: #a6e3a1;");
            consolaRapidaArea.setText("✓ Compilación completa exitosa: U1 Semántica, U2 Intermedio, U3 Optimización y U4 Objeto.");

            resultado.append("✓ ANÁLISIS SEMÁNTICO EXITOSO\n\n");
            resultado.append("No se detectaron inconsistencias de tipos ni variables no declaradas.");
        }

        resultadoArea.setText(resultado.toString());
        accionesArea.setText(String.join("\n", analizador.getAccionesSemanticas()));

        StringBuilder pila = new StringBuilder("Pila Semántica:\n\n");
        for (var tipo : analizador.getPilaSemantica()) {
            pila.append("│ ").append(tipo).append(" │\n");
        }
        pilaArea.setText(pila.toString());

        traduccionArea.setText(String.join("\n", analizador.getTraduccionIntermedia()));

        NodoExpresion arbolExpresion = analizador.getUltimoArbol();
        mostrarArbol(arbolExpresion);

        // 2. Generación U2
        generarCodigoIntermedio(arbolExpresion);
        generarEsquemaUnidad2(codigo);

        // 3. Optimización U3
        ejecutarOptimizaciones();

        // 4. Código Objeto U4 (Recibe el código optimizado de la U3)
        generarCodigoObjeto();
    }

    private void generarCodigoIntermedio(NodoExpresion arbol) {
        if (arbol == null) {
            limpiarSalidasUnidad2();
            return;
        }

        infijaArea.setText(generador.generarInfija(arbol));
        prefijaArea.setText(generador.generarPrefija(arbol));
        postfijaArea.setText(generador.generarPostfija(arbol));
        polacaArea.setText(generador.generarPolaca(arbol));
        codigoPArea.setText(generador.generarCodigoP(arbol));
        triplosArea.setText(generador.generarTriplos(arbol));
        cuadruplosArea.setText(generador.generarCuadruplos(arbol));
    }

    private void generarEsquemaUnidad2(String codigo) {
        generadorEsquema.generar(codigo);

        variablesArea.setText(String.join("\n", generadorEsquema.getVariables()));
        asignacionesArea.setText(String.join("\n", generadorEsquema.getAsignaciones()));
        controlesArea.setText(String.join("\n", generadorEsquema.getControles()));
        funcionesArea.setText(String.join("\n", generadorEsquema.getFunciones()));
        estructurasArea.setText(String.join("\n", generadorEsquema.getEstructuras()));

        StringBuilder completo = new StringBuilder();
        completo.append("=== CÓDIGO DE TRES DIRECCIONES ===\n\n");
        completo.append(String.join("\n", generadorEsquema.getTresDirecciones()));
        completo.append("\n\n=== CÓDIGO P ===\n\n");
        completo.append(String.join("\n", generadorEsquema.getCodigoP()));
        completo.append("\n\n=== TRIPLOS ===\n\n");
        completo.append(String.join("\n", generadorEsquema.getTriplos()));
        completo.append("\n\n=== CUÁDRUPLOS ===\n\n");
        completo.append(String.join("\n", generadorEsquema.getCuadruplos()));

        generacion23Area.setText(completo.toString());
    }

    private void ejecutarOptimizaciones() {
        optimizador.optimizar(generadorEsquema.getTresDirecciones());

        StringBuilder sbMirilla = new StringBuilder("=== BITÁCORA DE MIRILLA ===\n\n");
        sbMirilla.append(String.join("\n", optimizador.getBitacoraMirilla()));
        sbMirilla.append("\n\n=== CÓDIGO TRAS MIRILLA ===\n\n");
        sbMirilla.append(String.join("\n", optimizador.getCodigoMirilla()));
        mirillaArea.setText(sbMirilla.toString());

        StringBuilder sbBloques = new StringBuilder("=== GRAFO DE FLUJO DE CONTROL (CFG) ===\n\n");
        for (BloqueBasico b : optimizador.getBloquesBasicos()) {
            sbBloques.append(b.toString()).append("\n");
        }
        bloquesBasicosArea.setText(sbBloques.toString());

        StringBuilder sbLocal = new StringBuilder("=== BITÁCORA DE OPTIMIZACIÓN LOCAL ===\n\n");
        sbLocal.append(String.join("\n", optimizador.getBitacoraLocal()));
        sbLocal.append("\n\n=== CÓDIGO CON OPTIMIZACIÓN LOCAL ===\n\n");
        sbLocal.append(String.join("\n", optimizador.getCodigoLocal()));
        localArea.setText(sbLocal.toString());

        StringBuilder sbCiclos = new StringBuilder("=== BITÁCORA DE OPTIMIZACIÓN DE CICLOS ===\n\n");
        sbCiclos.append(String.join("\n", optimizador.getBitacoraCiclos()));
        sbCiclos.append("\n\n=== CÓDIGO TRAS CODE MOTION / BUCLES ===\n\n");
        sbCiclos.append(String.join("\n", optimizador.getCodigoCiclos()));
        ciclosArea.setText(sbCiclos.toString());

        StringBuilder sbGlobal = new StringBuilder("=== BITÁCORA GLOBAL & DEAD CODE ELIMINATION ===\n\n");
        sbGlobal.append(String.join("\n", optimizador.getBitacoraGlobal()));
        sbGlobal.append("\n\n=== CÓDIGO FINAL TOTALMENTE OPTIMIZADO ===\n\n");
        sbGlobal.append(String.join("\n", optimizador.getCodigoGlobal()));
        globalArea.setText(sbGlobal.toString());

        Map<String, Integer> antes = optimizador.calcularMetricas(optimizador.getCodigoOriginal());
        Map<String, Integer> despues = optimizador.calcularMetricas(optimizador.getCodigoGlobal());

        StringBuilder sbCostos = new StringBuilder("3.2 ESTIMACIÓN DE COSTOS DE EJECUCIÓN\n\n");
        sbCostos.append(String.format("%-32s | %-12s | %-12s | %-12s\n", "Métrica / Recurso", "Original", "Optimizado", "Ahorro"));
        sbCostos.append("-----------------------------------------------------------------------------\n");

        for (String metrica : antes.keySet()) {
            int vAntes = antes.get(metrica);
            int vDesp = despues.getOrDefault(metrica, 0);
            int ahorro = vAntes - vDesp;
            sbCostos.append(String.format("%-32s | %-12d | %-12d | %-12s\n",
                    metrica, vAntes, vDesp, (ahorro > 0 ? ("-" + ahorro + " (" + (ahorro * 100 / (vAntes == 0 ? 1 : vAntes)) + "%)") : "0%")));
        }
        costosArea.setText(sbCostos.toString());
    }

    // =========================================================
    // GENERACIÓN UNIDAD 4: CÓDIGO OBJETO
    // =========================================================

    private void generarCodigoObjeto() {
        memoria.limpiar();
        List<String> codigoOptimizado = optimizador.getCodigoGlobal();

        // 4.2 Ensamblador y 4.1 Registros
        List<String> programaAsm = generadorAsm.traducir(codigoOptimizado);
        ensambladorArea.setText(String.join("\n", programaAsm));
        registrosArea.setText(memoria.getEstadoRegistros());

        // 4.3 Lenguaje Máquina
        String maquina = mapeadorMaquina.generarLenguajeMaquina(programaAsm, memoria.getMapaOffsets());
        maquinaArea.setText(maquina);

        // 4.4 Administración de Memoria
        String mapaMem = memoria.generarMapaMemoria(mapeadorMaquina.getTotalBytes());
        memoriaArea.setText(mapaMem);
    }

    private void mostrarArbol(NodoExpresion nodo) {
        if (nodo == null) {
            arbol.setRoot(new TreeItem<>("Sin expresión disponible"));
            return;
        }
        TreeItem<String> raiz = construirNodo(nodo);
        arbol.setRoot(raiz);
        raiz.setExpanded(true);
    }

    private TreeItem<String> construirNodo(NodoExpresion nodo) {
        TreeItem<String> item = new TreeItem<>(nodo.getValor() + " [" + nodo.getTipo() + "]");
        if (nodo.getIzquierdo() != null) {
            item.getChildren().add(construirNodo(nodo.getIzquierdo()));
        }
        if (nodo.getDerecho() != null) {
            item.getChildren().add(construirNodo(nodo.getDerecho()));
        }
        item.setExpanded(true);
        return item;
    }

    private void limpiar() {
        codigoArea.clear();
        consolaRapidaArea.clear();
        resultadoArea.clear();
        accionesArea.clear();
        pilaArea.clear();
        traduccionArea.clear();
        limpiarSalidasUnidad2();

        mirillaArea.clear();
        bloquesBasicosArea.clear();
        localArea.clear();
        ciclosArea.clear();
        globalArea.clear();
        costosArea.clear();

        registrosArea.clear();
        ensambladorArea.clear();
        maquinaArea.clear();
        memoriaArea.clear();

        variablesArea.clear();
        asignacionesArea.clear();
        controlesArea.clear();
        funcionesArea.clear();
        estructurasArea.clear();
        generacion23Area.clear();
        tabla.getItems().clear();
        arbol.setRoot(null);
        generador.limpiar();
        generadorEsquema.limpiar();
        optimizador.limpiar();
        memoria.limpiar();
    }

    private void limpiarSalidasUnidad2() {
        infijaArea.clear();
        prefijaArea.clear();
        postfijaArea.clear();
        polacaArea.clear();
        codigoPArea.clear();
        triplosArea.clear();
        cuadruplosArea.clear();
    }

    private void cargarEjemploUnidad4() {
        codigoArea.setText("""
                int a = 10;
                int b = 5;
                int r = a + b;
                """);
    }

    private void cargarEjemploOptimizacion() {
        codigoArea.setText("""
                int a = 10;
                int b = 5;
                int c = 0;
                int x = a + 0;
                int y = b * 2;
                int z = a + b;
                int w = a + b;
                """);
    }

    private TextArea crearAreaSalida() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-control-inner-background: #181825; -fx-text-fill: #cdd6f4;");
        return area;
    }

    private VBox crearContenedorTab(String titulo, javafx.scene.Node contenido) {
        Label label = new Label(titulo);
        label.setStyle("-fx-text-fill: #89b4fa; -fx-font-size: 12px; -fx-font-weight: bold;");

        VBox box = new VBox(6, label, contenido);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #181825;");
        VBox.setVgrow(contenido, Priority.ALWAYS);
        return box;
    }

    private void aplicarEstilosGlobales(Scene scene) {
        String css = """
            .tab-pane .tab-header-area .tab-header-background {
                -fx-background-color: #11111b;
            }
            .tab {
                -fx-background-color: #1e1e2e;
                -fx-background-radius: 4 4 0 0;
            }
            .tab:selected {
                -fx-background-color: #313244;
                -fx-border-color: #89b4fa;
                -fx-border-width: 0 0 2 0;
            }
            .tab-label {
                -fx-text-fill: #a6adc8;
                -fx-font-size: 12px;
            }
            .tab:selected .tab-label {
                -fx-text-fill: #89b4fa;
                -fx-font-weight: bold;
            }
            .table-view {
                -fx-background-color: #181825;
                -fx-border-color: #313244;
                -fx-border-width: 1px;
            }
            .table-view .column-header-background {
                -fx-background-color: #11111b;
            }
            .table-view .column-header {
                -fx-background-color: #11111b;
                -fx-border-color: #313244;
                -fx-border-width: 0 1 1 0;
                -fx-padding: 6px;
            }
            .table-view .column-header .label {
                -fx-text-fill: #89b4fa;
                -fx-font-weight: bold;
            }
            .table-row-cell {
                -fx-background-color: #181825;
                -fx-border-color: #313244;
                -fx-border-width: 0 0 1 0;
            }
            .table-row-cell:odd {
                -fx-background-color: #1e1e2e;
            }
            .table-row-cell:filled:hover {
                -fx-background-color: #313244;
            }
            .table-row-cell:filled:selected {
                -fx-background-color: #45475a;
            }
            .table-view .table-cell {
                -fx-text-fill: #cdd6f4;
                -fx-alignment: CENTER-LEFT;
                -fx-padding: 0 8px;
            }
            .table-view .placeholder .label {
                -fx-text-fill: #6c7086;
            }
            .tree-view {
                -fx-background-color: #181825;
                -fx-control-inner-background: #181825;
                -fx-border-color: #313244;
                -fx-border-width: 1px;
            }
            .tree-cell {
                -fx-background-color: #181825;
                -fx-text-fill: #cdd6f4;
            }
            .tree-cell:filled:selected {
                -fx-background-color: #313244;
                -fx-text-fill: #89b4fa;
            }
            .split-pane-divider {
                -fx-background-color: #313244;
                -fx-padding: 1px;
            }
            .scroll-bar:vertical, .scroll-bar:horizontal {
                -fx-background-color: #11111b;
            }
            .scroll-bar .thumb {
                -fx-background-color: #45475a;
                -fx-background-radius: 4px;
            }
        """;
        scene.getStylesheets().add("data:text/css," + css.replace("\n", ""));
    }
}