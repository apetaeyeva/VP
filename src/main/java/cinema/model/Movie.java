package cinema.model;

/** Фильм в прокате. */
public class Movie {
    private int id;
    private String title;
    private String genre;
    private int duration;   // минуты
    private int ageLimit;   // 0, 6, 12, 16, 18
    private double price;   // базовая цена билета, ₸

    public Movie(int id, String title, String genre, int duration, int ageLimit, double price) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.ageLimit = ageLimit;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public int getAgeLimit() { return ageLimit; }
    public void setAgeLimit(int ageLimit) { this.ageLimit = ageLimit; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() { return title; }
}