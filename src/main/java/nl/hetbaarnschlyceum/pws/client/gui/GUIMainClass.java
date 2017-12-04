package nl.hetbaarnschlyceum.pws.client.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class GUIMainClass extends Application  {
    Stage window;

    public static void start()
    {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("PWS Client");
        window = primaryStage;
        LoginScreen.showLoginscreen(window);
    }
}
