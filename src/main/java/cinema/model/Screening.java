package cinema.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

/** Сеанс: фильм + дата + время + зал + занятые места (ключи вида "ряд-место"). */
public class Screening {
    /** Размер зала: 6 рядов по 8 мест. */
    public static final int ROWS = 6;
    public static final int COLS = 8;

    private int id;
    private int movieId;
    private LocalDate date;
    private LocalTime time;
    private String hall;
    private final Set<String> taken = new LinkedHashSet<>();

    public Screening(int id, int movieId, LocalDate date, LocalTime time, String hall) {
        this.id = id;
        this.movieId = movieId;
        this.date = date;
        this.time = time;
        this.hall = hall;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMovieId() { return movieId; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public String getHall() { return hall; }
    public Set<String> getTaken() { return taken; }

    public int getTotalSeats() { return ROWS * COLS; }
    public int getFreeSeats() { return getTotalSeats() - taken.size(); }
}