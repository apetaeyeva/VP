package cinema.model;

/** Тип билета и коэффициент к цене. */
public enum TicketType {
    ADULT("Взрослый", 1.0),
    STUDENT("Студенческий (−30%)", 0.7),
    CHILD("Детский (−50%)", 0.5);

    private final String title;
    private final double factor;

    TicketType(String title, double factor) {
        this.title = title;
        this.factor = factor;
    }

    public double getFactor() { return factor; }

    @Override
    public String toString() { return title; }
}