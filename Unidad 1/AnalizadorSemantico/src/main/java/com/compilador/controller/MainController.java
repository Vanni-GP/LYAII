package com.compilador.controller;

import com.compilador.model.ErrorSemantico;
import com.compilador.model.NodoExpresion;
import com.compilador.model.Simbolo;
import com.compilador.semantic.AnalizadorSemantico;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainController {

    private final AnalizadorSemantico analizador;

    private TextArea codigoArea;
    private TextArea resultadoArea;
    private TextArea accionesArea;
    private TextArea pilaArea;
    private TextArea traduccionArea;

    private TableView<Simbolo> tabla;

    private TreeView<String> arbol;

    public MainController() {

        analizador =
                new AnalizadorSemantico();
    }

    public void mostrar(Stage stage) {

        Label titulo =
                new Label(
                        "COMPILADOR — ANÁLISIS SEMÁNTICO"
                );

        titulo.setStyle(
                "-fx-font-size: 24px;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitulo =
                new Label(
                        "Vanni Gutierrez Perez"
                );

        VBox encabezado =
                new VBox(
                        5,
                        titulo,
                        subtitulo
                );

        encabezado.setAlignment(
                Pos.CENTER
        );

        // =====================================
        // CÓDIGO
        // =====================================

        Label codigoLabel =
                new Label("Código fuente:");

        codigoArea =
                new TextArea();

        codigoArea.setPromptText(
                "Escribe el código del lenguaje..."
        );

        codigoArea.setStyle(
                "-fx-font-family: Consolas;"
                        + "-fx-font-size: 14px;"
        );

        VBox panelCodigo =
                new VBox(
                        8,
                        codigoLabel,
                        codigoArea
                );

        VBox.setVgrow(
                codigoArea,
                Priority.ALWAYS
        );

        // =====================================
        // TABLA DE SÍMBOLOS
        // =====================================

        Label tablaLabel =
                new Label(
                        "Tabla de símbolos y direcciones:"
                );

        tabla =
                new TableView<>();

        TableColumn<Simbolo, String>
                nombre =
                new TableColumn<>(
                        "Nombre"
                );

        nombre.setCellValueFactory(
                d ->
                        new SimpleStringProperty(
                                d.getValue()
                                        .getNombre()
                        )
        );

        TableColumn<Simbolo, String>
                tipo =
                new TableColumn<>(
                        "Tipo"
                );

        tipo.setCellValueFactory(
                d ->
                        new SimpleStringProperty(
                                d.getValue()
                                        .getTipo()
                                        .toString()
                        )
        );

        TableColumn<Simbolo, String>
                valor =
                new TableColumn<>(
                        "Valor"
                );

        valor.setCellValueFactory(
                d ->
                        new SimpleStringProperty(
                                d.getValue()
                                        .getValor()
                        )
        );

        TableColumn<Simbolo, String>
                direccion =
                new TableColumn<>(
                        "Dirección"
                );

        direccion.setCellValueFactory(
                d ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        d.getValue()
                                                .getDireccion()
                                )
                        )
        );

        TableColumn<Simbolo, String>
                linea =
                new TableColumn<>(
                        "Línea"
                );

        linea.setCellValueFactory(
                d ->
                        new SimpleStringProperty(
                                String.valueOf(
                                        d.getValue()
                                                .getLinea()
                                )
                        )
        );

        TableColumn<Simbolo, String>
                inicializada =
                new TableColumn<>(
                        "Inicializada"
                );

        inicializada.setCellValueFactory(
                d ->
                        new SimpleStringProperty(
                                d.getValue()
                                        .isInicializada()
                                        ? "Sí"
                                        : "No"
                        )
        );

        tabla.getColumns().addAll(
                nombre,
                tipo,
                valor,
                direccion,
                linea,
                inicializada
        );

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        VBox panelTabla =
                new VBox(
                        8,
                        tablaLabel,
                        tabla
                );

        VBox.setVgrow(
                tabla,
                Priority.ALWAYS
        );

        // =====================================
        // ÁRBOL
        // =====================================

        Label arbolLabel =
                new Label(
                        "Árbol de expresión:"
                );

        arbol =
                new TreeView<>();

        arbol.setShowRoot(true);

        VBox panelArbol =
                new VBox(
                        8,
                        arbolLabel,
                        arbol
                );

        VBox.setVgrow(
                arbol,
                Priority.ALWAYS
        );

        // =====================================
        // RESULTADO
        // =====================================

        Label resultadoLabel =
                new Label(
                        "Resultado:"
                );

        resultadoArea =
                new TextArea();

        resultadoArea.setEditable(
                false
        );

        resultadoArea.setWrapText(
                true
        );

        VBox panelResultado =
                new VBox(
                        8,
                        resultadoLabel,
                        resultadoArea
                );

        // =====================================
        // ACCIONES SEMÁNTICAS
        // =====================================

        Label accionesLabel =
                new Label(
                        "Acciones semánticas:"
                );

        accionesArea =
                new TextArea();

        accionesArea.setEditable(
                false
        );

        VBox panelAcciones =
                new VBox(
                        8,
                        accionesLabel,
                        accionesArea
                );

        // =====================================
        // PILA
        // =====================================

        Label pilaLabel =
                new Label(
                        "Pila semántica:"
                );

        pilaArea =
                new TextArea();

        pilaArea.setEditable(
                false
        );

        VBox panelPila =
                new VBox(
                        8,
                        pilaLabel,
                        pilaArea
                );

        // =====================================
        // TRADUCCIÓN
        // =====================================

        Label traduccionLabel =
                new Label(
                        "Esquema de traducción / código intermedio:"
                );

        traduccionArea =
                new TextArea();

        traduccionArea.setEditable(
                false
        );

        VBox panelTraduccion =
                new VBox(
                        8,
                        traduccionLabel,
                        traduccionArea
                );

        // =====================================
        // PESTAÑAS
        // =====================================

        TabPane pestañas =
                new TabPane();

        Tab tabResultado =
                new Tab(
                        "Resultado",
                        panelResultado
                );

        Tab tabAcciones =
                new Tab(
                        "Acciones semánticas",
                        panelAcciones
                );

        Tab tabPila =
                new Tab(
                        "Pila semántica",
                        panelPila
                );

        Tab tabTraduccion =
                new Tab(
                        "Traducción",
                        panelTraduccion
                );

        pestañas.getTabs().addAll(
                tabResultado,
                tabAcciones,
                tabPila,
                tabTraduccion
        );

        pestañas.setTabClosingPolicy(
                TabPane.TabClosingPolicy.UNAVAILABLE
        );

        // =====================================
        // BOTONES
        // =====================================

        Button analizar =
                new Button(
                        "ANALIZAR"
                );

        Button limpiar =
                new Button(
                        "LIMPIAR"
                );

        Button ejemploCorrecto =
                new Button(
                        "EJEMPLO CORRECTO"
                );

        Button ejemploError =
                new Button(
                        "EJEMPLO CON ERROR"
                );

        analizar.setOnAction(
                e -> analizar()
        );

        limpiar.setOnAction(
                e -> limpiar()
        );

        ejemploCorrecto.setOnAction(
                e -> cargarEjemploCorrecto()
        );

        ejemploError.setOnAction(
                e -> cargarEjemploError()
        );

        HBox botones =
                new HBox(
                        10,
                        analizar,
                        limpiar,
                        ejemploCorrecto,
                        ejemploError
                );

        botones.setAlignment(
                Pos.CENTER
        );

        // =====================================
        // PARTE SUPERIOR
        // =====================================

        HBox superior =
                new HBox(
                        15,
                        panelCodigo,
                        panelTabla
                );

        HBox.setHgrow(
                panelCodigo,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                panelTabla,
                Priority.ALWAYS
        );

        // =====================================
        // PARTE CENTRAL
        // =====================================

        HBox central =
                new HBox(
                        15,
                        panelArbol,
                        pestañas
                );

        HBox.setHgrow(
                panelArbol,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                pestañas,
                Priority.ALWAYS
        );

        VBox root =
                new VBox(
                        15,
                        encabezado,
                        superior,
                        central,
                        botones
                );

        root.setPadding(
                new Insets(20)
        );

        VBox.setVgrow(
                superior,
                Priority.ALWAYS
        );

        VBox.setVgrow(
                central,
                Priority.ALWAYS
        );

        Scene scene =
                new Scene(
                        root,
                        1250,
                        800
                );

        stage.setTitle(
                "Analizador Semántico — Lenguajes y Autómatas II"
        );

        stage.setScene(scene);

        stage.show();
    }

    // ==========================================
    // ANALIZAR
    // ==========================================

    private void analizar() {

        String codigo =
                codigoArea.getText();

        if (codigo.trim().isEmpty()) {

            resultadoArea.setText(
                    "No se ha ingresado código."
            );

            return;
        }

        analizador.analizar(
                codigo
        );

        // Tabla
        tabla.getItems().clear();

        tabla.getItems().addAll(
                analizador
                        .getTablaSimbolos()
                        .values()
        );

        // Resultado
        StringBuilder resultado =
                new StringBuilder();

        if (analizador.hayErrores()) {

            resultado.append(
                    "✗ ANÁLISIS SEMÁNTICO CON ERRORES\n\n"
            );

            for (
                    ErrorSemantico error :
                    analizador.getErrores()
            ) {

                resultado.append(
                        "• "
                );

                resultado.append(
                        error
                );

                resultado.append(
                        "\n"
                );
            }

        } else {

            resultado.append(
                    "✓ ANÁLISIS SEMÁNTICO CORRECTO\n\n"
            );

            resultado.append(
                    "No se encontraron errores semánticos."
            );
        }

        resultadoArea.setText(
                resultado.toString()
        );

        // Acciones
        accionesArea.setText(
                String.join(
                        "\n",
                        analizador
                                .getAccionesSemanticas()
                )
        );

        // Pila
        StringBuilder pila =
                new StringBuilder();

        pila.append(
                "Estado final de la pila semántica:\n\n"
        );

        for (
                var tipo :
                analizador.getPilaSemantica()
        ) {

            pila.append(
                    "│ "
            );

            pila.append(
                    tipo
            );

            pila.append(
                    " │\n"
            );
        }

        pilaArea.setText(
                pila.toString()
        );

        // Traducción
        traduccionArea.setText(
                String.join(
                        "\n",
                        analizador
                                .getTraduccionIntermedia()
                )
        );

        // Árbol
        mostrarArbol(
                analizador.getUltimoArbol()
        );
    }

    // ==========================================
    // ÁRBOL
    // ==========================================

    private void mostrarArbol(
            NodoExpresion nodo
    ) {

        if (nodo == null) {

            arbol.setRoot(
                    new TreeItem<>(
                            "Sin expresión"
                    )
            );

            return;
        }

        TreeItem<String> raiz =
                construirNodo(nodo);

        arbol.setRoot(
                raiz
        );

        raiz.setExpanded(true);
    }

    private TreeItem<String> construirNodo(
            NodoExpresion nodo
    ) {

        TreeItem<String> item =
                new TreeItem<>(
                        nodo.getValor()
                                + " : "
                                + nodo.getTipo()
                );

        if (nodo.getIzquierdo() != null) {

            item.getChildren().add(
                    construirNodo(
                            nodo.getIzquierdo()
                    )
            );
        }

        if (nodo.getDerecho() != null) {

            item.getChildren().add(
                    construirNodo(
                            nodo.getDerecho()
                    )
            );
        }

        item.setExpanded(true);

        return item;
    }

    // ==========================================
    // LIMPIAR
    // ==========================================

    private void limpiar() {

        codigoArea.clear();

        resultadoArea.clear();

        accionesArea.clear();

        pilaArea.clear();

        traduccionArea.clear();

        tabla.getItems().clear();

        arbol.setRoot(null);
    }

    // ==========================================
    // EJEMPLO CORRECTO
    // ==========================================

    private void cargarEjemploCorrecto() {

        codigoArea.setText("""
                int edad = 21;
                int cantidad = 3;
                float precio = 50.5f;
                float total = precio * cantidad;
                int resultado = edad + cantidad * 2;
                edad = 25;
                """);
    }

    // ==========================================
    // EJEMPLO CON ERRORES
    // ==========================================

    private void cargarEjemploError() {

        codigoArea.setText("""
                int edad = 21;
                float precio = 50.5f;
                int resultado = edad + precio;
                edad = precio;
                nombre = edad;
                int edad = 30;
                """);
    }
}