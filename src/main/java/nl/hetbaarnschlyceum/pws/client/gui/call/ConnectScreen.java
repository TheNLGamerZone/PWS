package nl.hetbaarnschlyceum.pws.client.gui.call;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConnectScreen {
    public static Scene showCallscreen(Stage window, String calleduser){
        Label label = new Label("Verbinden met " + calleduser);
        Button buttonendcall = new Button("Ophangen");
        Button buttontoconversation = new Button("Ga door naar gesprek");
        buttontoconversation.setOnAction(e -> ConversationScreen.showConversationscreen(window, calleduser));
        buttonendcall.setOnAction(e -> EndCallScreen.showEndofcallscreen(window,calleduser));

        VBox layoutconnectscreen = new VBox();

        layoutconnectscreen.getChildren().addAll(label,buttonendcall,buttontoconversation);
        layoutconnectscreen.setAlignment(Pos.CENTER);
        layoutconnectscreen.setSpacing(6);
        Scene connectscreen = new Scene(layoutconnectscreen, 800,500);
        window.setScene(connectscreen);
        window.show();

        return connectscreen;
    }
}

