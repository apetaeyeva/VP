package cinema.util;

import cinema.model.TicketType;

/** Расчёт стоимости билетов (вынесен отдельно, чтобы его можно было тестировать). */
public final class PriceCalculator {
    /** Наценка за VIP-ряд (последний ряд зала). */
    public static final double VIP_FACTOR = 1.5;

    private PriceCalculator() { }

    /** Цена одного билета, округлённая до целых тенге. */
    public static long seatPrice(double basePrice, int row, int totalRows, TicketType type) {
        double price = basePrice * type.getFactor();
        if (row == totalRows) {
            price *= VIP_FACTOR;
        }
        return Math.round(price);
    }
}