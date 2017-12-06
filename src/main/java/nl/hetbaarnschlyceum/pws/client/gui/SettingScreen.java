package nl.hetbaarnschlyceum.pws.client.gui;



import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingScreen {
    public static Scene showSettingsscreen(Stage window){
        //Linkerdeel vd instellingen
        VBox linksinstellingen = new VBox();
        Label labelpwreset = new Label("Wachtwoord resetten");
        TextField tfpassword = new TextField();
        tfpassword.setPromptText("Nieuwe wachtwoord");
        tfpassword.setMaxSize(200,6);
        TextField tfrepeatpassword = new TextField();
        tfrepeatpassword.setPromptText("Herhaal wachtwoord");
        tfrepeatpassword.setMaxSize(200,6);
        TextField tfoldpassword = new TextField();
        tfoldpassword.setPromptText("Oud wachtwoord");
        tfoldpassword.setMaxSize(200,6);
        Button buttonresetpassword = new Button("Reset");
        buttonresetpassword.setOnAction(e -> LoginScreen.showLoginscreen(window));
        Label spacerlabel = new Label(" ");
        Label labelstatus = new Label("Status");
        ChoiceBox<String> cbstatus = new ChoiceBox<>();
        cbstatus.getItems().addAll("Online", "Offline", "Niet storen", "AFK", "Onzichtbaar");
        cbstatus.setValue("Online");
        Button buttonresetstatus = new Button("Reset");

        Label spacerlabel2 = new Label("");
        Label labelcontacts = new Label("Adresboek");
        ChoiceBox<String> cbcontacts = new ChoiceBox<>();
        cbcontacts.getItems().addAll("DDDDDD", "EEEEEEE","FFFFFF","000000");

        cbcontacts.setValue("DDDDD");
        Button buttonresetcontact = new Button("Reset");
        buttonresetcontact.setOnAction(e -> cbcontacts.setValue("000000"));

        linksinstellingen.getChildren().addAll(labelpwreset,tfpassword,tfrepeatpassword,tfoldpassword,buttonresetpassword,spacerlabel,labelstatus,cbstatus,buttonresetstatus,spacerlabel2,labelcontacts,cbcontacts,buttonresetcontact);
        linksinstellingen.setPrefWidth(400);
        linksinstellingen.setSpacing(10);
        linksinstellingen.setAlignment(Pos.TOP_CENTER);

        //rechterdeel vd instellingen
        VBox rechtsinstellingen = new VBox();
        Label RSAlabel = new Label("RSA");
        RSAlabel.setMaxSize(300,400);
        Label Whitelistlabel = new Label("Whitelist enzo");
        Whitelistlabel.setMaxSize(300,500);
        rechtsinstellingen.getChildren().addAll(RSAlabel,Whitelistlabel);
        rechtsinstellingen.setSpacing(50);
        rechtsinstellingen.setMinSize(400,250);
        rechtsinstellingen.setAlignment(Pos.TOP_LEFT);
        //onder
        HBox onder = new HBox();
        Button buttontohomescreen = new Button("Terug naar het hoofdscherm");
        buttontohomescreen.setOnAction(e -> MainScreen.showMainscreen(window));
        onder.setAlignment(Pos.CENTER);
        onder.getChildren().add(buttontohomescreen);
        onder.setMinHeight(100);
        BorderPane layoutsettingsscreen = new BorderPane();
        layoutsettingsscreen.setBottom(onder);
        layoutsettingsscreen.setLeft(linksinstellingen);
        layoutsettingsscreen.setRight(rechtsinstellingen);
        Scene settingsscreen = new Scene(layoutsettingsscreen, 800, 500);
        window.setScene(settingsscreen);
        window.show();

        return settingsscreen;

    }
}

