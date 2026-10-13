package cinema.data;

import cinema.model.Movie;
import cinema.model.Screening;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

/**
 * Хранилище данных приложения. Данные лежат в двух CSV-файлах
 * в папке ~/.cinema-booking (разделитель — точка с запятой).
 * Если файлов нет или они повреждены — создаются демонстрационные данные.
 */
public final class DataStore {
    public static final List<String> HALLS = List.of("Зал 1", "Зал 2", "Зал 3 (IMAX)");

    private static final DataStore INSTANCE = new DataStore();

    private final ObservableList<Movie> movies = FXCollections.observableArrayList();
    private final List<Screening> screenings = new ArrayList<>();
    private final Path dir = Paths.get(System.getProperty("user.home"), ".cinema-booking");
    private final Path moviesFile = dir.resolve("movies.csv");
    private final Path screeningsFile = dir.resolve("screenings.csv");
    private int nextMovieId = 1;
    private int nextScreeningId = 1;
    private String lastError;

    private DataStore() {
        load();
    }

    public static DataStore getInstance() { return INSTANCE; }

    public ObservableList<Movie> getMovies() { return movies; }

    /** Текст последней ошибки ввода-вывода или null. */
    public String getLastError() { return lastError; }

    // ---------- Фильмы ----------

    public void addMovie(Movie movie) {
        movie.setId(nextMovieId++);
        movies.add(movie);
        save();
    }

    public void removeMovie(Movie movie) {
        movies.remove(movie);
        screenings.removeIf(s -> s.getMovieId() == movie.getId());
        save();
    }

    public List<String> getGenres() {
        TreeSet<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Movie m : movies) {
            set.add(m.getGenre());
        }
        return new ArrayList<>(set);
    }

    // ---------- Сеансы ----------

    public List<Screening> getScreeningsFor(Movie movie) {
        List<Screening> result = new ArrayList<>();
        if (movie == null) {
            return result;
        }
        for (Screening s : screenings) {
            if (s.getMovieId() == movie.getId()) {
                result.add(s);
            }
        }
        result.sort(Comparator.comparing(Screening::getDate).thenComparing(Screening::getTime));
        return result;
    }

    public void addScreening(Screening screening) {
        screening.setId(nextScreeningId++);
        screenings.add(screening);
        save();
    }

    public void removeScreening(Screening screening) {
        screenings.remove(screening);
        save();
    }

    public int getScreeningsCount() { return screenings.size(); }

    public int getTicketsSold() {
        int n = 0;
        for (Screening s : screenings) {
            n += s.getTaken().size();
        }
        return n;
    }

    // ---------- Сохранение / загрузка ----------

    /** @return true, если данные успешно записаны. */
    public boolean save() {
        try {
            Files.createDirectories(dir);
            List<String> m = new ArrayList<>();
            for (Movie mv : movies) {
                m.add(mv.getId() + ";" + mv.getTitle() + ";" + mv.getGenre() + ";"
                        + mv.getDuration() + ";" + mv.getAgeLimit() + ";" + mv.getPrice());
            }
            Files.write(moviesFile, m, StandardCharsets.UTF_8);

            List<String> s = new ArrayList<>();
            for (Screening sc : screenings) {
                s.add(sc.getId() + ";" + sc.getMovieId() + ";" + sc.getDate() + ";" + sc.getTime()
                        + ";" + sc.getHall() + ";" + String.join(",", sc.getTaken()));
            }
            Files.write(screeningsFile, s, StandardCharsets.UTF_8);
            lastError = null;
            return true;
        } catch (IOException e) {
            lastError = "Не удалось сохранить данные: " + e.getMessage();
            return false;
        }
    }

    private void load() {
        movies.clear();
        screenings.clear();
        if (!Files.exists(moviesFile) || !Files.exists(screeningsFile)) {
            seedDemo();
            return;
        }
        try {
            for (String line : Files.readAllLines(moviesFile, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] p = line.split(";", -1);
                Movie m = new Movie(Integer.parseInt(p[0]), p[1], p[2],
                        Integer.parseInt(p[3]), Integer.parseInt(p[4]), Double.parseDouble(p[5]));
                movies.add(m);
                nextMovieId = Math.max(nextMovieId, m.getId() + 1);
            }
            for (String line : Files.readAllLines(screeningsFile, StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                String[] p = line.split(";", -1);
                Screening sc = new Screening(Integer.parseInt(p[0]), Integer.parseInt(p[1]),
                        LocalDate.parse(p[2]), LocalTime.parse(p[3]), p[4]);
                if (p.length > 5 && !p[5].isBlank()) {
                    for (String key : p[5].split(",")) {
                        sc.getTaken().add(key);
                    }
                }
                screenings.add(sc);
                nextScreeningId = Math.max(nextScreeningId, sc.getId() + 1);
            }
        } catch (IOException | RuntimeException e) {
            // повреждённый файл не должен ронять приложение
            lastError = "Файлы данных повреждены, загружены демо-данные (" + e.getMessage() + ")";
            seedDemo();
        }
    }

    /** Сбрасывает всё к демонстрационным данным. */
    public void resetDemo() {
        seedDemo();
    }

    private void seedDemo() {
        movies.clear();
        screenings.clear();
        nextMovieId = 1;
        nextScreeningId = 1;

        Object[][] demo = {
                {"Начало", "Фантастика", 148, 12, 2500.0},
                {"Интерстеллар", "Фантастика", 169, 12, 2700.0},
                {"Матрица", "Боевик", 136, 16, 2300.0},
                {"Джокер", "Драма", 122, 18, 2500.0},
                {"Паразиты", "Триллер", 132, 16, 2400.0},
                {"Дюна", "Фантастика", 155, 12, 2800.0},
                {"Унесённые призраками", "Мультфильм", 125, 6, 1800.0},
                {"Король Лев", "Мультфильм", 88, 0, 1700.0},
                {"Побег из Шоушенка", "Драма", 142, 16, 2200.0},
                {"Гладиатор", "Приключения", 155, 16, 2500.0},
                {"Хатико", "Драма", 93, 6, 1900.0},
                {"Один дома", "Комедия", 103, 6, 1800.0},
        };
        for (Object[] d : demo) {
            movies.add(new Movie(nextMovieId++, (String) d[0], (String) d[1],
                    (Integer) d[2], (Integer) d[3], (Double) d[4]));
        }

        LocalTime[] times = {LocalTime.of(12, 0), LocalTime.of(15, 30), LocalTime.of(19, 0), LocalTime.of(21, 30)};
        Random rnd = new Random(42);
        int k = 0;
        for (Movie m : movies) {
            for (int i = 0; i < 3; i++) {
                Screening s = new Screening(nextScreeningId++, m.getId(),
                        LocalDate.now().plusDays(i % 3), times[(k + i) % times.length],
                        HALLS.get((k + i) % HALLS.size()));
                int busy = rnd.nextInt(14);
                for (int b = 0; b < busy; b++) {
                    s.getTaken().add((1 + rnd.nextInt(Screening.ROWS)) + "-" + (1 + rnd.nextInt(Screening.COLS)));
                }
                screenings.add(s);
            }
            k++;
        }
        save();
    }
}