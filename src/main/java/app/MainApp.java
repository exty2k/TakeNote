package app;

import controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.MainViewController;

import java.io.IOException;

public class MainApp extends Application {

    /** Заполняется классом Main перед вызовом launch(), т.к. JavaFX сам создаёт экземпляр MainApp. */
    private static Controller controller;
    private static String connectionStatusText = "не подключено";

    public static void inject(Controller controllerInstance, String statusText) {
        controller = controllerInstance;
        connectionStatusText = statusText;
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();

        MainViewController viewController = loader.getController();
        viewController.init(controller, connectionStatusText);

        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style.css").toExternalForm());

        stage.setTitle("TakeNote");
        stage.setMinWidth(820);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();
    }
}
