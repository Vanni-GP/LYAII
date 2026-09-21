package com.compilador;

import com.compilador.controller.MainController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        MainController controller =
                new MainController();

        controller.mostrar(stage);
    }

    public static void main(String[] args) {

        launch(args);
    }
}