package cinema;

import cinema.data.DataStore;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/cinema/main.fxml"));
        stage.setTitle("Кинотеатр — продажа билетов");
        stage.setScene(new Scene(root));
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setOnCloseRequest(e -> DataStore.getInstance().save());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
