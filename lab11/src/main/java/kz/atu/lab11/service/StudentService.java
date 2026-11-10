package kz.atu.lab11.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import kz.atu.lab11.model.Student;

import java.util.List;

public class StudentService {

    private final ObservableList<Student> students =
            FXCollections.observableArrayList();

    public ObservableList<Student> getStudents() {
        return students;
    }

    public void addStudent(String n, String g, double p) {
        validate(n, g, p);
        if (isDuplicate(n, g, null)) {
            throw new IllegalArgumentException("Такой студент уже существует");
        }
        students.add(new Student(n.trim(), g.trim(), p));
    }

    public void updateStudent(Student s, String n, String g, double p) {
        if (s == null) throw new IllegalArgumentException("Студент не выбран");
        validate(n, g, p);
        if (isDuplicate(n, g, s)) {
            throw new IllegalArgumentException("Такой студент уже существует");
        }
        s.setName(n.trim());
        s.setGroup(g.trim());
        s.setGpa(p);
    }

    public void deleteStudent(Student s) {
        if (s == null) throw new IllegalArgumentException("Студент не выбран");
        students.remove(s);
    }

    /** Средний GPA всех студентов (0, если список пуст). */
    public double averageGpa() {
        return students.stream().mapToDouble(Student::getGpa).average().orElse(0.0);
    }

    /** Динамический список групп, сформированный из данных студентов. */
    public List<String> getGroups() {
        return students.stream()
                .map(Student::getGroup)
                .distinct()
                .sorted()
                .toList();
    }

    private boolean isDuplicate(String n, String g, Student except) {
        String name = n.trim();
        String group = g.trim();
        return students.stream().anyMatch(s -> s != except
                && s.getName().equalsIgnoreCase(name)
                && s.getGroup().equalsIgnoreCase(group));
    }

    private void validate(String n, String g, double p) {
        if (n == null || n.isBlank()) throw new IllegalArgumentException("Введите ФИО");
        if (g == null || g.isBlank()) throw new IllegalArgumentException("Введите группу");
        if (!Double.isFinite(p) || p < 0 || p > 4)
            throw new IllegalArgumentException("GPA должен быть от 0 до 4");
    }
}
