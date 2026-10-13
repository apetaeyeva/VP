package cinema.controller;

import cinema.data.DataStore;
import cinema.model.Movie;
import cinema.model.Screening;
import cinema.model.TicketType;
import cinema.util.PriceCalculator;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Окно выбора мест и расчёта стоимости билетов. */
public class SeatController {
    @FXML private Label titleLabel;
    @FXML private Label infoLabel;
    @FXML private Label chosenLabel;
    @FXML private Label totalLabel;
    @FXML private GridPane seatGrid;
    @FXML private ComboBox<TicketType> ticketCombo;
    @FXML private Button confirmButton;

    private Movie movie;
    private Screening screening;
    private DataStore store;
    private final Set<String> chosen = new LinkedHashSet<>();
    private int soldCount;

    @FXML
    private void initialize() {
        ticketCombo.getItems().setAll(TicketType.values());
        ticketCombo.getSelectionModel().selectFirst();
        ticketCombo.valueProperty().addListener((obs, o, n) -> recalc());
        confirmButton.setDisable(true);
    }

    public void setData(Movie movie, Screening screening, DataStore store) {
        this.movie = movie;
        this.screening = screening;
        this.store = store;
        titleLabel.setText(movie.getTitle());
        infoLabel.setText(screening.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + "  "
                + screening.getTime().format(DateTimeFormatter.ofPattern("HH:mm")) + "  ·  " + screening.getHall()
                + "  ·  цена от " + String.format("%.0f", movie.getPrice()) + " ₸");
        buildGrid();
        recalc();
    }

    /** Сколько билетов было куплено в этом окне. */
    public int getSoldCount() { return soldCount; }

    private void buildGrid() {
        seatGrid.getChildren().clear();
        for (int row = 1; row <= Screening.ROWS; row++) {
            Label rowLabel = new Label("Ряд " + row);
            GridPane.setHalignment(rowLabel, HPos.RIGHT);
            seatGrid.add(rowLabel, 0, row - 1);
            for (int col = 1; col <= Screening.COLS; col++) {
                final String key = row + "-" + col;
                ToggleButton seat = new ToggleButton(String.valueOf(col));
                seat.getStyleClass().add("seat");
                if (row == Screening.ROWS) {
                    seat.getStyleClass().add("seat-vip");
                }
                if (screening.getTaken().contains(key)) {
                    seat.getStyleClass().add("seat-taken");
                    seat.setDisable(true);
                } else {
                    seat.setOnAction(e -> {
                        if (seat.isSelected()) {
                            chosen.add(key);
                        } else {
                            chosen.remove(key);
                        }
                        recalc();
                    });
                }
                seatGrid.add(seat, col, row - 1);
            }
        }
    }

    private void recalc() {
        if (movie == null) {
            return;
        }
        TicketType type = ticketCombo.getValue();
        long total = 0;
        List<String> names = new ArrayList<>();
        for (String key : chosen) {
            String[] p = key.split("-");
            int row = Integer.parseInt(p[0]);
            total += PriceCalculator.seatPrice(movie.getPrice(), row, Screening.ROWS, type);
            names.add("ряд " + p[0] + " место " + p[1]);
        }
        chosenLabel.setText(chosen.isEmpty() ? "Выберите места на схеме зала."
                : "Выбрано (" + chosen.size() + "): " + String.join(", ", names));
        totalLabel.setText("Итого: " + total + " ₸");
        confirmButton.setDisable(chosen.isEmpty());
    }

    @FXML
    private void onConfirm() {
        if (chosen.isEmpty()) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Билетов: " + chosen.size() + "\n" + totalLabel.getText(), ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Подтвердить покупку?");
        if (confirm.showAndWait().filter(b -> b == ButtonType.OK).isEmpty()) {
            return;
        }
        screening.getTaken().addAll(chosen);
        soldCount = chosen.size();
        if (!store.save()) {
            Alert err = new Alert(Alert.AlertType.ERROR, store.getLastError(), ButtonType.OK);
            err.setHeaderText("Билеты куплены, но данные не сохранены");
            err.showAndWait();
        } else {
            Alert ok = new Alert(Alert.AlertType.INFORMATION,
                    movie.getTitle() + "\n" + infoLabel.getText().split("  ·  цена")[0]
                            + "\n" + chosenLabel.getText() + "\n" + totalLabel.getText(), ButtonType.OK);
            ok.setHeaderText("Покупка завершена");
            ok.showAndWait();
        }
        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        ((Stage) seatGrid.getScene().getWindow()).close();
    }
}