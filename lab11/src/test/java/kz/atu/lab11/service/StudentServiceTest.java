package kz.atu.lab11.service;

import kz.atu.lab11.model.Student;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentServiceTest {

    // --- тесты из методички ---

    @Test
    void shouldAddStudent() {
        StudentService s = new StudentService();
        s.addStudent("Алия", "ИС-301", 3.7);
        assertEquals(1, s.getStudents().size());
    }

    @Test
    void shouldRejectInvalidGpa() {
        StudentService s = new StudentService();
        assertThrows(IllegalArgumentException.class,
                () -> s.addStudent("Алия", "ИС-301", 5.0));
    }

    // --- собственные тесты ---

    @Test
    void shouldRejectBlankName() {
        StudentService s = new StudentService();
        assertThrows(IllegalArgumentException.class,
                () -> s.addStudent("  ", "ИС-301", 3.0));
    }

    @Test
    void shouldRejectDuplicate() {
        StudentService s = new StudentService();
        s.addStudent("Алия", "ИС-301", 3.7);
        assertThrows(IllegalArgumentException.class,
                () -> s.addStudent(" алия ", "ис-301", 3.0));
        assertEquals(1, s.getStudents().size());
    }

    @Test
    void shouldAllowSameNameInOtherGroup() {
        StudentService s = new StudentService();
        s.addStudent("Алия", "ИС-301", 3.7);
        s.addStudent("Алия", "ИС-302", 3.2);
        assertEquals(2, s.getStudents().size());
    }

    @Test
    void shouldUpdateStudent() {
        StudentService s = new StudentService();
        s.addStudent("Алия", "ИС-301", 3.7);
        Student st = s.getStudents().get(0);
        s.updateStudent(st, "Алия Н.", "ИС-302", 3.9);
        assertEquals("Алия Н.", st.getName());
        assertEquals("ИС-302", st.getGroup());
        assertEquals(3.9, st.getGpa(), 1e-9);
    }

    @Test
    void shouldAllowUpdateWithoutChangingKey() {
        StudentService s = new StudentService();
        s.addStudent("Алия", "ИС-301", 3.7);
        Student st = s.getStudents().get(0);
        assertDoesNotThrow(() -> s.updateStudent(st, "Алия", "ИС-301", 4.0));
    }

    @Test
    void shouldFailUpdateOrDeleteWithoutSelection() {
        StudentService s = new StudentService();
        assertThrows(IllegalArgumentException.class, () -> s.updateStudent(null, "A", "B", 1));
        assertThrows(IllegalArgumentException.class, () -> s.deleteStudent(null));
    }

    @Test
    void shouldDeleteStudent() {
        StudentService s = new StudentService();
        s.addStudent("Алия", "ИС-301", 3.7);
        s.deleteStudent(s.getStudents().get(0));
        assertTrue(s.getStudents().isEmpty());
    }

    @Test
    void shouldCalculateAverageGpa() {
        StudentService s = new StudentService();
        assertEquals(0.0, s.averageGpa(), 1e-9);
        s.addStudent("А", "ИС-301", 3.0);
        s.addStudent("Б", "ИС-301", 4.0);
        assertEquals(3.5, s.averageGpa(), 1e-9);
    }

    @Test
    void shouldBuildDynamicGroupList() {
        StudentService s = new StudentService();
        s.addStudent("А", "ИС-302", 3.0);
        s.addStudent("Б", "ИС-301", 3.0);
        s.addStudent("В", "ИС-301", 3.0);
        assertEquals(List.of("ИС-301", "ИС-302"), s.getGroups());
    }

    @Test
    void shouldRejectNaNAndNegativeGpa() {
        StudentService s = new StudentService();
        assertThrows(IllegalArgumentException.class, () -> s.addStudent("А", "Г", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> s.addStudent("А", "Г", -0.1));
    }
}
