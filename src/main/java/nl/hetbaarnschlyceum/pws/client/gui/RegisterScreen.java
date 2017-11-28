package nl.hetbaarnschlyceum.pws.client.gui;


import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;


public class RegisterScreen {
    public static Scene showRegisterscreen(Stage window){
        Button registerbutton = new Button("Registreren");
        int thoog = 4;

        Button buttontoinlog = new Button("Terug naar Inloggen");
        TextField tfname = new TextField();
        tfname.setPromptText("Naam");
        TextField tfnumber = new TextField();
        tfnumber.setPromptText("nr");
        TextField tfpassword = new TextField();
        tfpassword.setPromptText("Wachtwoord");
        TextField tfpassword2 = new TextField();
        tfpassword2.setPromptText("Herhalen");
        Alert registrated = new Alert(Alert.AlertType.INFORMATION);
        registrated.setContentText("Je bent geregistreerd!");
        Alert alreadyRegistrated = new Alert(Alert.AlertType.INFORMATION);
        alreadyRegistrated.setContentText("Je wachtwoorden komen niet overeen");

        //registerbutton.setOnAction(e -> {
        //if (tfname.getText().contains("_&2d") || tfname.getText().contains("&") || tfname.getText().contains("=") || tfnumber.getText().contains("_&2d") || tfnumber.getText().contains("&") || tfnumber.getText().contains("=") || tfpassword.getText().contains("_&2d") || tfpassword.getText().contains("&") || tfpassword.getText().contains("=") || tfpassword2.getText().contains("_&2d") || tfpassword2.getText().contains("&") || tfpassword2.getText().contains("=")) {
        // tfname.setText(" ");
        //tfnumber.setText(" ");
        //foutmelding.show();
        //}
        //else {
        //if (tfpassword.getText().equals(tfpassword2.getText())){
        //registeredname[0] = tfname.getText();
        //registerednr[0] = tfnumber.getText();
        //registeredpw[0] = tfpassword.getText();
        //window.setScene(inlogscherm);
        //registrated.show();
        //}
        //else{

        //}
        //}


        //});
        Label labelname = new Label("Naam:");
        Label labelpassword2 = new Label("Wachtwoord herhalen:");
        Label labelpassword = new Label("Wachtwoord: ");
        Label headerlabel = new Label("Registreren");
        Label labelnr = new Label("Gewenst nummer:");
        buttontoinlog.setOnAction(e -> LoginScreen.showLoginscreen(window));
        registerbutton.setLayoutX(370);
        registerbutton.setLayoutY(310);
        buttontoinlog.setLayoutX(650);
        buttontoinlog.setLayoutY(450);
        tfname.setLayoutX(370);
        tfname.setLayoutY(190);
        tfname.setPrefWidth(100);
        tfname.setMaxHeight(thoog);
        tfnumber.setLayoutX(370);
        tfnumber.setLayoutY(220);
        tfnumber.setPrefWidth(100);
        tfnumber.setMaxHeight(thoog);
        tfpassword.setLayoutX(370);
        tfpassword.setLayoutY(250);
        tfpassword.setPrefWidth(100);
        tfpassword.setMaxHeight(thoog);
        tfpassword2.setLayoutX(370);
        tfpassword2.setLayoutY(280);
        tfpassword2.setPrefWidth(100);
        tfpassword2.setPrefHeight(thoog);
        labelname.setLayoutX(330);
        labelname.setLayoutY(197);
        labelnr.setLayoutX(258);
        labelnr.setLayoutY(225);
        labelpassword2.setLayoutX(230);
        labelpassword2.setLayoutY(280);
        labelpassword.setLayoutX(288);
        labelpassword.setLayoutY(254);
        headerlabel.setLayoutX(370);
        headerlabel.setLayoutY(160);
        Pane layoutregisterscreen = new Pane();
        layoutregisterscreen.getChildren().addAll(registerbutton,buttontoinlog,tfname,tfnumber,tfpassword,tfpassword2,labelname,labelpassword2,labelpassword,headerlabel,labelnr);
        Scene registerscreen = new Scene(layoutregisterscreen,800, 500);
        window.setTitle("Registreren");
        window.setScene(registerscreen);
        window.show();
        return registerscreen;
    }
}
