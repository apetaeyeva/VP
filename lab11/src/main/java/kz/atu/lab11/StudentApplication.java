package kz.atu.lab11;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StudentApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                StudentApplication.class.getResource("student-view.fxml"));
        Scene scene = new Scene(loader.load(), 760, 520);
        stage.setTitle("Каталог студентов");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
