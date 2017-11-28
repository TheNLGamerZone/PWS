package nl.hetbaarnschlyceum.pws.client.gui;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import nl.hetbaarnschlyceum.pws.client.Client;

import java.util.Arrays;

public class LoginScreen {

    public static Scene showLoginscreen(Stage window){
        int thoog = 4;
        Button loginbutton = new Button("Inloggen");
        Button registerbutton = new Button("Registreren");
        TextField textfieldname = new TextField();
        Alert foutmelding = new Alert(Alert.AlertType.ERROR);
        foutmelding.setContentText("De volgende karakters zijn niet toegestaan: " +
                Arrays.toString(Client.forbiddenStrings));
        TextField textfieldpassword = new TextField();
        Label labelname = new Label("Naam:");
        Label labelpassword = new Label("Wachtwoord:");
        Label headerlabel = new Label("Inloggen");

        Alert notRegistered = new Alert(Alert.AlertType.INFORMATION);
        notRegistered.setContentText("Inloggen vanwege BETA");


        final String[] loginname = new String[1];
        final String[] loginpassword = new String[1];
        loginbutton.setOnAction(event -> {
            if(Arrays.stream(Client.forbiddenStrings).parallel().anyMatch(textfieldname.getText()::contains))
            {
                textfieldname.setText("");
                foutmelding.show();
            }
            else {
                loginname[0] = textfieldname.getText();
                loginpassword[0] = textfieldpassword.getText();
                MainScreen.showMainscreen(window, loginname[0]);
            }
        });
        registerbutton.setOnAction(e -> RegisterScreen.showRegisterscreen(window));

        textfieldname.setLayoutX(360);
        textfieldname.setLayoutY(218);
        textfieldname.setPrefWidth(100);
        textfieldname.setMaxHeight(thoog);
        textfieldpassword.setLayoutX(360);
        textfieldpassword.setLayoutY(250);
        textfieldpassword.setPrefWidth(100);
        textfieldpassword.setMaxHeight(thoog);
        loginbutton.setLayoutX(360);
        loginbutton.setLayoutY(285);
        registerbutton.setLayoutX(670);
        registerbutton.setLayoutY(430);
        labelname.setLayoutX(320);
        labelname.setLayoutY(222);
        labelpassword.setLayoutX(278);
        labelpassword.setLayoutY(254);
        headerlabel.setLayoutX(360);
        headerlabel.setLayoutY(190);

        Pane layoutloginscreen = new Pane();
        layoutloginscreen.getChildren().addAll(loginbutton,registerbutton,textfieldname,textfieldpassword,labelname,labelpassword,headerlabel);
        Scene loginscreen = new Scene(layoutloginscreen,800, 500);
        window.setTitle("Inloggen");
        window.setScene(loginscreen);
        window.show();

        return loginscreen;
    }
}
