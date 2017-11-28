package nl.hetbaarnschlyceum.pws.client.GUI;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Connectscreen {
    public static Scene showCallscreen(Stage window, String calleduser){
        Label label = new Label("Verbinden met " + calleduser);
        Button buttonendcall = new Button("Ophangen");
        Button buttontoconversation = new Button("Ga door naar gesprek");
        buttontoconversation.setOnAction(e -> Conversationscreen.showConversationscreen(window, calleduser));
        buttonendcall.setOnAction(e -> Endofcallscreen.showEndofcallscreen(window,calleduser));

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

