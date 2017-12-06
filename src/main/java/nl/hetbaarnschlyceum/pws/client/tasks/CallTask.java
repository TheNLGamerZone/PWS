package nl.hetbaarnschlyceum.pws.client.tasks;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import nl.hetbaarnschlyceum.pws.server.tc.OperationResult;
import nl.hetbaarnschlyceum.pws.server.tc.Request;

import static nl.hetbaarnschlyceum.pws.PWS.print;

public class CallTask extends Task
{
    private Alert alert;
    public CallTask(Alert alert) {
        this.alert = alert;
    }

    @Override
    public void setRequest(String request) {
        this.request = request;
    }

    @Override
    public void run()
    {
        if (request.split(Request.argumentSeperator)[0].equals("CALL_REQUEST_RSL"))
        {
            int callCode = Integer.valueOf(request.split(Request.argumentSeperator)[1]);

            if (callCode == OperationResult.SUCCESS_CALLING) {
                print("[INFO] Gesprek wordt gestart..");
            } else {
                print("[INFO] %s", OperationResult.errorMessages[callCode]);
                Platform.runLater(() -> {
                    alert.getButtonTypes().add(ButtonType.CANCEL);
                    alert.close();

                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText(OperationResult.errorMessages[callCode]);
                    alert.show();
                });
            }
        }
    }
}
