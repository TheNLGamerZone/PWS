package nl.hetbaarnschlyceum.pws.client.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import nl.hetbaarnschlyceum.pws.client.Client;
import nl.hetbaarnschlyceum.pws.client.gui.call.CallScreen;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Objects;


public class MainScreen {
    public static Scene showMainscreen(Stage window){
        //linkerdeel vh hoofdscherm
        Label labelcontacts = new Label("Contacten");
        TextField tfsearchuser = new TextField();
        tfsearchuser.setPromptText("Gebruiker zoeken");
        tfsearchuser.setMaxSize(250,5);
        ListView lvcontacts = new ListView();
        lvcontacts.setMaxSize(250,370);
        int aantalcontacten = 2;
        String[] ArrayGebelde = new String[aantalcontacten];
        ArrayGebelde[0] = "Tim Anema";
        ArrayGebelde[1] = "Michiel van den Eshof";

        lvcontacts.getItems().addAll(ArrayGebelde[0],ArrayGebelde[1]);
        final String[] userInCall = {" "};
        Button buttoncallup = new Button("Bel op");

        //rechterdeel vh hoofdscherm
        Label labelloggedin = new Label("Ingelogd als " + Client.username);
        Button buttonsettings = new Button(" Instellingen");
        Button buttonlogout = new Button("Uitloggen");
        Label labelcallnumber = new Label("Nummer Bellen");
        TextField tfnumber = new TextField();
        tfnumber.setMaxSize(150,5);
        tfnumber.setPromptText("Nummer");
        Button buttoncallnumber = new Button("Bellen");

        Label labelfailedcalls = new Label("Mislukte inlogpogingen");
        ListView lvfailedcalls = new ListView();
        lvfailedcalls.setMaxSize(250,150);
        Label labelincomingcall = new Label("Binnenkomende oproepen");
        ListView lvincomingcalls = new ListView();
        lvincomingcalls.setMaxSize(250,100);
        Button buttonacceptcall = new Button("Accepteer inkomende oproep");

        String call1 = "Tim Anema 12345";
        String nocall = "Geen";
        boolean call = true;
        if(call){
            lvincomingcalls.getItems().addAll(call1);
        }
        else{
            lvincomingcalls.getItems().add(nocall);
        }

        // DEMO BUTTONS - START
        // GUIMainClass.demoAlert.show()
        buttoncallup.setOnAction(e -> GUIMainClass.demoAlert.show()
                //CallScreen.showCallscreen(window)
        );

        buttoncallnumber.setOnAction(e -> GUIMainClass.demoAlert.show()
                //CallScreen.showCallscreen(window)
        );

        buttonsettings.setOnAction(e -> GUIMainClass.demoAlert.show()
                //SettingScreen.showSettingsscreen(window)
        );

        buttonlogout.setOnAction(e -> GUIMainClass.demoAlert.show()
                //LoginScreen.showLoginscreen(window)
        );

        buttonacceptcall.setOnAction(e -> GUIMainClass.demoAlert.show()
                //CallScreen.showCallscreen(window)
        );
        // DEMO BUTTONS - END

        tfnumber.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*"))
            {
                tfnumber.setText(newValue.replaceAll("[^\\d]", ""));
            }

            if (StringUtils.isNumeric(tfnumber.getText()))
            {
                String formatted = Long.valueOf(tfnumber.getText()).toString();

                tfnumber.setText(formatted);
            }

            if (tfnumber.getText().length() > 9)
            {
                String s = tfnumber.getText().substring(0, 9);
                tfnumber.setText(s);
            }
        });

        buttoncallnumber.setOnAction(event -> {
            String targetNumber = tfnumber.getText().trim();

            if (targetNumber.equals("")
                    || !StringUtils.isNumeric(targetNumber)
                    || targetNumber.length() != 9)
            {
                Alert emptyAlert = new Alert(Alert.AlertType.ERROR);

                tfnumber.setText("");
                emptyAlert.setContentText("Het nummer moet een geldig nummer zijn!");
                emptyAlert.show();
            } else
            {
                int target = Integer.valueOf(targetNumber);

                Client.startCall(target);
            }
        });

        BorderPane borderpane = new BorderPane();
        VBox linksmenu = new VBox();
        linksmenu.setPrefWidth(400);
        linksmenu.setAlignment(Pos.TOP_CENTER);
        linksmenu.setSpacing(10);
        linksmenu.getChildren().addAll(labelcontacts,tfsearchuser,lvcontacts,buttoncallup);

        VBox rechtsmenu = new VBox();
        rechtsmenu.setPrefWidth(400);
        rechtsmenu.setSpacing(8);
        rechtsmenu.setAlignment(Pos.TOP_CENTER);
        rechtsmenu.getChildren().addAll(labelloggedin,buttonsettings,buttonlogout,labelcallnumber,tfnumber,buttoncallnumber,labelfailedcalls,lvfailedcalls,labelincomingcall,lvincomingcalls,buttonacceptcall);
        borderpane.setLeft(linksmenu);
        borderpane.setRight(rechtsmenu);
        Scene mainscreen = new Scene(borderpane,800,500);
        window.setScene(mainscreen);
        window.show();

        return mainscreen;
    }
}
