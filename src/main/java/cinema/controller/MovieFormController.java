package cinema.controller;

import cinema.model.Movie;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/** Форма добавления / редактирования фильма. */
public class MovieFormController {
    @FXML private Label headerLabel;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> genreCombo;
    @FXML private TextField durationField;
    @FXML private ComboBox<Integer> ageCombo;
    @FXML private TextField priceField;

    private Movie movie;    // null — режим добавления
    private Movie result;   // не null после успешного сохранения

    @FXML
    private void initialize() {
        ageCombo.getItems().setAll(0, 6, 12, 16, 18);
        ageCombo.setValue(12);
    }

    public void setMovie(Movie movie, List<String> genres) {
        this.movie = movie;
        genreCombo.getItems().setAll(genres);
        if (movie == null) {
            headerLabel.setText("Новый фильм");
            return;
        }
        headerLabel.setText("Редактирование фильма");
        titleField.setText(movie.getTitle());
        genreCombo.setValue(movie.getGenre());
        durationField.setText(String.valueOf(movie.getDuration()));
        ageCombo.setValue(movie.getAgeLimit());
        priceField.setText(String.format("%.0f", movie.getPrice()));
    }

    public Movie getResult() { return result; }

    @FXML
    private void onSave() {
        List<String> errors = new ArrayList<>();

        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        if (title.isEmpty()) {
            errors.add("• Введите название фильма.");
        } else if (title.length() > 60) {
            errors.add("• Название не должно быть длиннее 60 символов.");
        } else if (title.contains(";")) {
            errors.add("• Название не должно содержать символ «;».");
        }

        String genre = genreCombo.getEditor().getText() == null ? "" : genreCombo.getEditor().getText().trim();
        if (genre.isEmpty()) {
            errors.add("• Укажите жанр.");
        } else if (genre.contains(";") || genre.length() > 30) {
            errors.add("• Жанр: не более 30 символов, без символа «;».");
        }

        int duration = 0;
        try {
            duration = Integer.parseInt(durationField.getText().trim());
            if (duration < 1 || duration > 400) {
                errors.add("• Длительность должна быть от 1 до 400 минут.");
            }
        } catch (NumberFormatException e) {
            errors.add("• Длительность должна быть целым числом.");
        }

        double price = 0;
        try {
            price = Double.parseDouble(priceField.getText().trim().replace(',', '.'));
            if (!(price > 0 && price <= 100000)) {
                errors.add("• Цена должна быть больше 0 и не больше 100 000 ₸.");
            }
        } catch (NumberFormatException e) {
            errors.add("• Цена должна быть числом, например 2500.");
        }

        Integer age = ageCombo.getValue();
        if (age == null) {
            errors.add("• Выберите возрастное ограничение.");
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, String.join("\n", errors), ButtonType.OK);
            alert.setHeaderText("Проверьте введённые данные");
            alert.showAndWait();
            return;                         // форму не закрываем
        }

        if (movie == null) {
            result = new Movie(0, title, genre, duration, age, price);
        } else {
            movie.setTitle(title);
            movie.setGenre(genre);
            movie.setDuration(duration);
            movie.setAgeLimit(age);
            movie.setPrice(price);
            result = movie;
        }
        close();
    }

    @FXML
    private void onCancel() {
        result = null;
        close();
    }

    private void close() {
        ((Stage) titleField.getScene().getWindow()).close();
    }
}