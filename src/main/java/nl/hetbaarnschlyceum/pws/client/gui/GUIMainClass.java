package nl.hetbaarnschlyceum.pws.client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import nl.hetbaarnschlyceum.pws.PWS;
import nl.hetbaarnschlyceum.pws.client.Client;
import nl.hetbaarnschlyceum.pws.client.ConnectionThread;

import java.awt.event.ActionEvent;

import static nl.hetbaarnschlyceum.pws.PWS.print;

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

    @Override
    public void stop()
    {
        print("[INFO] De client wordt gesloten");

        if (Client.connectionEstablished) {
            ConnectionThread.processedRequestFromServer(
                    ConnectionThread.prepareMessage(PWS.MessageIdentifier.DISCONNECT)
            );
        }

        System.exit(0);
    }

    @FXML
    public void exitApplication(ActionEvent event)
    {
        Platform.exit();
    }
}
