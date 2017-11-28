package nl.hetbaarnschlyceum.pws.client.gui.call;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nl.hetbaarnschlyceum.pws.client.gui.MainScreen;

public class EndCallScreen {
    public static Scene showEndofcallscreen(Stage window, String calledUser){
        Label label = new Label("Je hebt de verbinding verbroken met " + calledUser);
        Button button = new Button("Klik hierop als je terug wil naar het hoofdscherm");
        button.setOnAction(e -> MainScreen.showMainscreen(window, "TODO: fixen"));

        VBox layoutendofcallscreen = new VBox();
        layoutendofcallscreen.getChildren().addAll(label,button);
        layoutendofcallscreen.setAlignment(Pos.CENTER);
        layoutendofcallscreen.setSpacing(6);
        Scene endofcallscreen = new Scene(layoutendofcallscreen, 800, 500);
        window.setScene(endofcallscreen);
        window.show();

        return endofcallscreen;
    }
}
