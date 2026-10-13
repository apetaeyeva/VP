package cinema.controller;

import cinema.data.DataStore;
import cinema.model.Movie;
import cinema.model.Screening;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Главное окно: таблица фильмов, список сеансов, поиск и фильтр по жанру. */
public class MainController {
    private static final String ALL_GENRES = "Все жанры";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private TableView<Movie> movieTable;
    @FXML private TableColumn<Movie, String> colTitle;
    @FXML private TableColumn<Movie, String> colGenre;
    @FXML private TableColumn<Movie, Number> colDuration;
    @FXML private TableColumn<Movie, Number> colAge;
    @FXML private TableColumn<Movie, Number> colPrice;
    @FXML private ListView<Screening> sessionList;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> genreFilter;
    @FXML private Label sessionsTitle;
    @FXML private Label statusLabel;
    @FXML private Label statsLabel;

    private final DataStore store = DataStore.getInstance();
    private FilteredList<Movie> filtered;

    @FXML
    private void initialize() {
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colGenre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGenre()));
        colDuration.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDuration()));
        colAge.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getAgeLimit()));
        colPrice.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrice()));
        colDuration.setCellFactory(col -> numberCell(" мин"));
        colAge.setCellFactory(col -> numberCell("+"));
        colPrice.setCellFactory(col -> numberCell(" ₸"));

        filtered = new FilteredList<>(store.getMovies(), m -> true);
        SortedList<Movie> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(movieTable.comparatorProperty());
        movieTable.setItems(sorted);
        movieTable.setPlaceholder(new Label("Фильмы не найдены"));

        refreshGenres();
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        genreFilter.valueProperty().addListener((obs, o, n) -> applyFilter());

        movieTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> refreshSessions());
        movieTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && selectedMovie() != null) {
                onEditMovie();
            }
        });

        sessionList.setPlaceholder(new Label("Выберите фильм, чтобы увидеть сеансы"));
        sessionList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Screening s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                } else {
                    setText(s.getDate().format(DATE_FMT) + "  " + s.getTime().format(TIME_FMT)
                            + "  ·  " + s.getHall()
                            + "  ·  свободно " + s.getFreeSeats() + "/" + s.getTotalSeats());
                }
            }
        });
        sessionList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                onBook();
            }
        });

        movieTable.getSelectionModel().selectFirst();
        if (store.getLastError() != null) {
            setStatus(store.getLastError());
        } else {
            setStatus("Готово");
        }
        updateStats();
    }

    // ---------- Фильмы ----------

    @FXML
    private void onAddMovie() {
        Movie created = showMovieForm(null);
        if (created != null) {
            store.addMovie(created);
            refreshGenres();
            movieTable.getSelectionModel().select(created);
            movieTable.scrollTo(created);
            setStatus("Фильм «" + created.getTitle() + "» добавлен");
            updateStats();
        }
    }

    @FXML
    private void onEditMovie() {
        Movie m = selectedMovie();
        if (m == null) {
            warn("Выберите фильм в таблице.");
            return;
        }
        Movie edited = showMovieForm(m);
        if (edited != null) {
            saveOrWarn();
            movieTable.refresh();
            refreshGenres();
            refreshSessions();
            setStatus("Фильм «" + edited.getTitle() + "» изменён");
        }
    }

    @FXML
    private void onDeleteMovie() {
        Movie m = selectedMovie();
        if (m == null) {
            warn("Выберите фильм в таблице.");
            return;
        }
        int sessions = store.getScreeningsFor(m).size();
        if (confirm("Удалить фильм «" + m.getTitle() + "»?",
                "Вместе с ним будут удалены сеансы (" + sessions + ") и проданные билеты.")) {
            store.removeMovie(m);
            refreshGenres();
            refreshSessions();
            setStatus("Фильм «" + m.getTitle() + "» удалён");
            updateStats();
        }
    }

    // ---------- Сеансы и билеты ----------

    @FXML
    private void onBook() {
        Movie m = selectedMovie();
        Screening s = sessionList.getSelectionModel().getSelectedItem();
        if (m == null || s == null) {
            warn("Выберите фильм и сеанс, на который хотите купить билеты.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cinema/seats.fxml"));
            Parent root = loader.load();
            SeatController controller = loader.getController();
            controller.setData(m, s, store);
            Stage stage = new Stage();
            stage.setTitle("Выбор мест — " + m.getTitle());
            stage.initOwner(movieTable.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            int sold = controller.getSoldCount();
            if (sold > 0) {
                setStatus("Продано билетов: " + sold + " (" + m.getTitle() + ")");
            }
            refreshSessions(s);
            updateStats();
        } catch (IOException e) {
            error("Не удалось открыть окно выбора мест: " + e.getMessage());
        }
    }

    @FXML
    private void onAddSession() {
        Movie m = selectedMovie();
        if (m == null) {
            warn("Сначала выберите фильм.");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Новый сеанс");
        dialog.setHeaderText("Фильм: " + m.getTitle());
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField timeField = new TextField("18:00");
        timeField.setPromptText("ЧЧ:ММ");
        ComboBox<String> hallBox = new ComboBox<>(FXCollections.observableArrayList(DataStore.HALLS));
        hallBox.getSelectionModel().selectFirst();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.addRow(0, new Label("Дата:"), datePicker);
        grid.addRow(1, new Label("Время (ЧЧ:ММ):"), timeField);
        grid.addRow(2, new Label("Зал:"), hallBox);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            String err = null;
            if (datePicker.getValue() == null) {
                err = "Укажите дату сеанса.";
            } else if (datePicker.getValue().isBefore(LocalDate.now())) {
                err = "Нельзя создать сеанс в прошлом.";
            } else if (parseTime(timeField.getText()) == null) {
                err = "Время должно быть в формате ЧЧ:ММ, например 18:30.";
            }
            if (err != null) {
                ev.consume();       // не закрываем диалог, пока ввод неверный
                warn(err);
            }
        });

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            Screening s = new Screening(0, m.getId(), datePicker.getValue(),
                    parseTime(timeField.getText()), hallBox.getValue());
            store.addScreening(s);
            refreshSessions(s);
            setStatus("Сеанс добавлен");
            updateStats();
        }
    }

    @FXML
    private void onDeleteSession() {
        Screening s = sessionList.getSelectionModel().getSelectedItem();
        if (s == null) {
            warn("Выберите сеанс в списке.");
            return;
        }
        String extra = s.getTaken().isEmpty() ? "" : "Проданные билеты (" + s.getTaken().size() + ") будут потеряны.";
        if (confirm("Удалить сеанс " + s.getDate().format(DATE_FMT) + " " + s.getTime().format(TIME_FMT) + "?", extra)) {
            store.removeScreening(s);
            refreshSessions();
            setStatus("Сеанс удалён");
            updateStats();
        }
    }

    // ---------- Фильтры ----------

    @FXML
    private void onResetFilter() {
        searchField.clear();
        genreFilter.setValue(ALL_GENRES);
    }

    @FXML
    private void onFocusSearch() {
        searchField.requestFocus();
        searchField.selectAll();
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String g = genreFilter.getValue();
        filtered.setPredicate(m ->
                (g == null || ALL_GENRES.equals(g) || m.getGenre().equalsIgnoreCase(g))
                        && (q.isEmpty() || m.getTitle().toLowerCase().contains(q)));
        if (movieTable.getSelectionModel().getSelectedItem() == null) {
            movieTable.getSelectionModel().selectFirst();
        }
        updateStats();
    }

    private void refreshGenres() {
        String selected = genreFilter.getValue();
        List<String> items = new ArrayList<>();
        items.add(ALL_GENRES);
        items.addAll(store.getGenres());
        genreFilter.setItems(FXCollections.observableArrayList(items));
        genreFilter.setValue(items.contains(selected) ? selected : ALL_GENRES);
    }

    // ---------- Меню ----------

    @FXML
    private void onSave() {
        if (store.save()) {
            setStatus("Данные сохранены");
        } else {
            error(store.getLastError());
        }
    }

    @FXML
    private void onResetDemo() {
        if (confirm("Сбросить данные?", "Все фильмы, сеансы и проданные билеты будут заменены демонстрационными.")) {
            store.resetDemo();
            refreshGenres();
            onResetFilter();
            movieTable.getSelectionModel().selectFirst();
            refreshSessions();
            setStatus("Загружены демонстрационные данные");
            updateStats();
        }
    }

    @FXML
    private void onExit() {
        store.save();
        Platform.exit();
    }

    @FXML
    private void onAbout() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("О программе");
        a.setHeaderText("Cinema Ticket Booking");
        a.setContentText("Мини-проект СРО №1 по дисциплине «Визуальное программирование».\n"
                + "Java 17+, JavaFX, FXML, Scene Builder.\n\n"
                + "Горячие клавиши: Ctrl+N — новый фильм, Ctrl+E — изменить, Ctrl+D — удалить, "
                + "Ctrl+B — купить билеты, Ctrl+T — новый сеанс, Ctrl+F — поиск, Ctrl+S — сохранить.");
        a.showAndWait();
    }

    // ---------- Вспомогательные методы ----------

    private Movie showMovieForm(Movie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cinema/movie_form.fxml"));
            Parent root = loader.load();
            MovieFormController controller = loader.getController();
            controller.setMovie(movie, store.getGenres());
            Stage stage = new Stage();
            stage.setTitle(movie == null ? "Новый фильм" : "Редактирование фильма");
            stage.initOwner(movieTable.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            return controller.getResult();
        } catch (IOException e) {
            error("Не удалось открыть форму: " + e.getMessage());
            return null;
        }
    }

    private Movie selectedMovie() {
        return movieTable.getSelectionModel().getSelectedItem();
    }

    private void refreshSessions() {
        refreshSessions(null);
    }

    private void refreshSessions(Screening toSelect) {
        Movie m = selectedMovie();
        sessionList.setItems(FXCollections.observableArrayList(store.getScreeningsFor(m)));
        sessionsTitle.setText(m == null ? "Сеансы" : "Сеансы: " + m.getTitle());
        if (toSelect != null && sessionList.getItems().contains(toSelect)) {
            sessionList.getSelectionModel().select(toSelect);
        } else {
            sessionList.getSelectionModel().selectFirst();
        }
    }

    private void updateStats() {
        statsLabel.setText("Фильмов: " + filtered.size() + " из " + store.getMovies().size()
                + "   |   Сеансов: " + store.getScreeningsCount()
                + "   |   Продано билетов: " + store.getTicketsSold());
    }

    private void saveOrWarn() {
        if (!store.save()) {
            warn(store.getLastError());
        }
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    private static LocalTime parseTime(String text) {
        try {
            return LocalTime.parse(text.trim(), DateTimeFormatter.ofPattern("H:mm"));
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    private static TableCell<Movie, Number> numberCell(String suffix) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else if (value instanceof Double) {
                    setText(String.format("%.0f", value.doubleValue()) + suffix);
                } else {
                    setText(value.intValue() + suffix);
                }
            }
        };
    }

    private void warn(String text) {
        Alert a = new Alert(Alert.AlertType.WARNING, text, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void error(String text) {
        Alert a = new Alert(Alert.AlertType.ERROR, text, ButtonType.OK);
        a.setHeaderText("Ошибка");
        a.showAndWait();
    }

    private boolean confirm(String header, String text) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(header);
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
}