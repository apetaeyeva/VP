package kz.atu.lab11.controller;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import kz.atu.lab11.model.Student;
import kz.atu.lab11.service.StudentService;

import java.util.Locale;

public class StudentController {

    private static final String ALL = "Все";

    @FXML private TableView<Student> tableStudents;
    @FXML private TableColumn<Student, String> colName, colGroup;
    @FXML private TableColumn<Student, Number> colGpa;
    @FXML private TextField txtName, txtGroup, txtGpa, txtSearch;
    @FXML private ComboBox<String> cmbGroupFilter;
    @FXML private Label lblCount, lblAvg;

    private final StudentService service = new StudentService();
    private FilteredList<Student> filtered;

    @FXML
    private void initialize() {
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colGroup.setCellValueFactory(c -> c.getValue().groupProperty());
        colGpa.setCellValueFactory(c -> c.getValue().gpaProperty());

        service.addStudent("Иванов Иван", "ИС-301", 3.5);
        service.addStudent("Петров Пётр", "ИС-302", 3.8);

        filtered = new FilteredList<>(service.getStudents(), s -> true);
        SortedList<Student> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tableStudents.comparatorProperty());
        tableStudents.setItems(sorted);

        refreshGroups();

        txtSearch.textProperty().addListener((o, a, b) -> applyFilter());
        cmbGroupFilter.valueProperty().addListener((o, a, b) -> applyFilter());

        // выбор строки -> заполнение полей для редактирования
        tableStudents.getSelectionModel().selectedItemProperty().addListener((o, a, s) -> {
            if (s != null) {
                txtName.setText(s.getName());
                txtGroup.setText(s.getGroup());
                txtGpa.setText(String.valueOf(s.getGpa()));
            }
        });

        applyFilter();
    }

    /** Обновляет список групп в ComboBox, сохраняя выбранное значение. */
    private void refreshGroups() {
        String current = cmbGroupFilter.getValue();
        cmbGroupFilter.getItems().setAll(ALL);
        cmbGroupFilter.getItems().addAll(service.getGroups());
        if (current != null && cmbGroupFilter.getItems().contains(current)) {
            cmbGroupFilter.setValue(current);
        } else {
            cmbGroupFilter.setValue(ALL);
        }
    }

    private void applyFilter() {
        String q = txtSearch.getText() == null ? ""
                : txtSearch.getText().trim().toLowerCase(Locale.ROOT);
        String group = cmbGroupFilter.getValue();
        filtered.setPredicate(s -> s.getName().toLowerCase(Locale.ROOT).contains(q)
                && (group == null || group.equals(ALL) || s.getGroup().equals(group)));
        updateCount();
    }

    private void updateCount() {
        lblCount.setText("Найдено студентов: " + filtered.size());
        lblAvg.setText(String.format(Locale.US, "Средний GPA: %.2f", service.averageGpa()));
    }

    private double readGpa() {
        try {
            return Double.parseDouble(txtGpa.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Введите числовой GPA");
        }
    }

    @FXML
    private void onAdd() {
        try {
            service.addStudent(txtName.getText(), txtGroup.getText(), readGpa());
            refreshGroups();
            applyFilter();
            clearFields();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        try {
            Student s = tableStudents.getSelectionModel().getSelectedItem();
            service.updateStudent(s, txtName.getText(), txtGroup.getText(), readGpa());
            refreshGroups();
            applyFilter();
            tableStudents.refresh();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        try {
            Student s = tableStudents.getSelectionModel().getSelectedItem();
            if (s == null) throw new IllegalArgumentException("Студент не выбран");
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Удалить студента «" + s.getName() + "»?", ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Подтверждение");
            confirm.setHeaderText(null);
            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
            service.deleteStudent(s);
            refreshGroups();
            applyFilter();
            clearFields();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void clearFields() {
        txtName.clear();
        txtGroup.clear();
        txtGpa.clear();
        tableStudents.getSelectionModel().clearSelection();
    }

    private void showError(String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Ошибка");
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }
}
