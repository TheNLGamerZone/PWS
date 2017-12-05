package nl.hetbaarnschlyceum.pws.client.gui;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class GUIMainClass extends Application  {
    public static Stage window;
    public static Alert demoAlert;

    public static void start()
    {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        demoAlert = new Alert(Alert.AlertType.INFORMATION);
        demoAlert.setContentText("Deze functie is niet beschikbaar in de demo-versie van de PWS-Client");
        primaryStage.setTitle("PWS Client (Demo)");
        window = primaryStage;
        window.setResizable(false);
        LoginScreen.showLoginscreen(window);
    }
}
