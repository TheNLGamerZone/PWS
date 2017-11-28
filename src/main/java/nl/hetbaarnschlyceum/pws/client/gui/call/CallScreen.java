package nl.hetbaarnschlyceum.pws.client.gui.call;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CallScreen {
    public static Scene showCallscreen(Stage window){
        String calleduser = "<gebruikersnaam>";
        Label Labelcalleduser = new Label("Bellen met" + calleduser );
        Button buttonstopcall = new Button("Ophangen");
        Button buttontoconnection = new Button("Ga door naar verbinden");
        buttontoconnection.setOnAction(e -> ConnectScreen.showCallscreen(window, calleduser));
        buttonstopcall.setOnAction(e -> EndCallScreen.showEndofcallscreen(window,calleduser));

        VBox layoutcallscreen = new VBox();
        layoutcallscreen.getChildren().addAll(Labelcalleduser,buttonstopcall,buttontoconnection);
        layoutcallscreen.setAlignment(Pos.CENTER);
        layoutcallscreen.setSpacing(6);
        Scene callscreen = new Scene(layoutcallscreen, 800, 500);
        window.setScene(callscreen);
        window.show();

        return callscreen;
    }
}
