package nl.hetbaarnschlyceum.pws.client.gui.call;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConversationScreen {
    public static Scene showConversationscreen(Stage window, String calleduser){
        Label label = new Label("In gesprek met " + calleduser);
        Button button = new Button("Ophangen");
        button.setOnAction(e -> EndCallScreen.showEndofcallscreen(window,calleduser));

        VBox layoutconversationscreen = new VBox();
        layoutconversationscreen.getChildren().addAll(label,button);
        layoutconversationscreen.setAlignment(Pos.CENTER);
        layoutconversationscreen.setSpacing(6);
        Scene conversationscreen = new Scene(layoutconversationscreen, 800, 500);
        window.setScene(conversationscreen);
        window.show();

        return conversationscreen;
    }
}
