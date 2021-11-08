package currencychart;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Optional;

// библиотека разбора JSON - www.java2s.com/Code/JarDownload/gson/gson-2.2.2.jar.zip

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        // график
        BorderPane root = FXMLLoader.load(getClass().getResource("currencychart.fxml"));
        primaryStage.setTitle("Изменение курса валют");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    static float getString_Float(String string)
    {
        String string_new  = string.replace(",", ".");
        if (string_new.isEmpty()) { return 0; }
        try {
            return Float.parseFloat(string_new);
        } catch (Exception e) {
            return 0;
        }
    }

    // проверка значения - float
    public static boolean checkString_Float(String string)
    {
        String string_new  = string.replace(",", ".");
        if (!string_new.isEmpty()) {
            try {
                Float.parseFloat(string_new);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    // вывод диалогового окна
    static void MessageBoxError(String infoMessage, String infoHeader)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка:");
        alert.setHeaderText(infoHeader);
        alert.setContentText(infoMessage);
        alert.showAndWait();
    }

    // вывод диалогового окна - Да/Нет
    static boolean MessageBoxYesNo(String infoMessage, String infoHeader)
    {
        //alert.showAndWait();
        Alert alert = new Alert(Alert.AlertType.WARNING, "", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Предупреждение:");
        alert.setHeaderText(infoHeader);
        alert.setContentText(infoMessage);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.YES) {
            return true;
        }
        return false;
    }
}
