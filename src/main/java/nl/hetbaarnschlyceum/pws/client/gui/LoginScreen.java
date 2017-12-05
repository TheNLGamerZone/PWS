package nl.hetbaarnschlyceum.pws.client.gui;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import nl.hetbaarnschlyceum.pws.client.Client;
import nl.hetbaarnschlyceum.pws.crypto.Hash;

import java.util.Arrays;

public class LoginScreen {
    static Button loginButton;
    static Button registerButton;
    static TextField textFieldName;
    static PasswordField passwordField;

    static Scene showLoginscreen(Stage window){
        int thoog = 4;
        loginButton = new Button("Inloggen");
        registerButton = new Button("Registreren");
        textFieldName = new TextField();
        Alert foutmelding = new Alert(Alert.AlertType.ERROR);
        foutmelding.setContentText("De volgende karakters zijn niet toegestaan: " +
                Arrays.toString(Client.forbiddenStrings));
        passwordField = new PasswordField();
        Label labelname = new Label("Naam:");
        Label labelpassword = new Label("Wachtwoord:");
        Label headerlabel = new Label("Inloggen");

        Alert notRegistered = new Alert(Alert.AlertType.INFORMATION);
        notRegistered.setContentText("Inloggen vanwege BETA");

        loginButton.setOnAction(event -> {
            if(Arrays.stream(Client.forbiddenStrings).parallel().anyMatch(textFieldName.getText()::contains)
                    || Arrays.stream(Client.forbiddenStrings).parallel().anyMatch(passwordField.getText()::contains))
            {
                textFieldName.setText("");
                passwordField.setText("");
                foutmelding.show();
            } else if (textFieldName.getText().length() < 6
                    || passwordField.getText().length() < 8)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.setContentText("Gebruikersnaam moet minimaal 6 karakters zijn!" +
                        "\nWachtwoord moet minimaal 8 karakters zijn!");
                alert.show();
            } else
            {
                toggleControls();

                Client.username = textFieldName.getText();
                Client.hashedPassword = Hash.generateHash(passwordField.getText());

                if (!Client.initConnection(Client.username, Client.hashedPassword, false))
                {
                    Alert alert = new Alert(Alert.AlertType.ERROR);

                    alert.setContentText("Kon niet verbinden met de server");
                    alert.show();
                    toggleControls();
                }
            }
        });
        registerButton.setOnAction(e ->
                GUIMainClass.demoAlert.show()
                //RegisterScreen.showRegisterscreen(window)
        );

        textFieldName.setLayoutX(360);
        textFieldName.setLayoutY(218);
        textFieldName.setPrefWidth(100);
        textFieldName.setMaxHeight(thoog);
        passwordField.setLayoutX(360);
        passwordField.setLayoutY(250);
        passwordField.setPrefWidth(100);
        passwordField.setMaxHeight(thoog);
        loginButton.setLayoutX(360);
        loginButton.setLayoutY(285);
        registerButton.setLayoutX(670);
        registerButton.setLayoutY(430);
        labelname.setLayoutX(320);
        labelname.setLayoutY(222);
        labelpassword.setLayoutX(278);
        labelpassword.setLayoutY(254);
        headerlabel.setLayoutX(360);
        headerlabel.setLayoutY(190);

        Pane layoutloginscreen = new Pane();
        layoutloginscreen.getChildren().addAll(loginButton,registerButton,textFieldName,passwordField,labelname,labelpassword,headerlabel);
        Scene loginscreen = new Scene(layoutloginscreen,800, 500);
        window.setScene(loginscreen);
        window.show();

        return loginscreen;
    }

    public static void toggleControls()
    {
        if (!loginButton.isDisabled())
        {
            loginButton.setDisable(true);
            registerButton.setDisable(true);
            textFieldName.setDisable(true);
            passwordField.setDisable(true);
            loginButton.setText("Verbinding maken..");
        } else
        {
            loginButton.setDisable(false);
            registerButton.setDisable(false);
            textFieldName.setDisable(false);
            passwordField.setDisable(false);
            loginButton.setText("Inloggen");
        }
    }
}
