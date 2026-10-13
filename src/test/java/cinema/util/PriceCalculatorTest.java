package cinema.util;

import cinema.model.TicketType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculatorTest {

    @Test
    void adultTicketCostsBasePrice() {
        assertEquals(2500, PriceCalculator.seatPrice(2500, 3, 6, TicketType.ADULT));
    }

    @Test
    void childTicketIsHalfPrice() {
        assertEquals(1250, PriceCalculator.seatPrice(2500, 3, 6, TicketType.CHILD));
    }

    @Test
    void studentTicketHasThirtyPercentDiscount() {
        assertEquals(1750, PriceCalculator.seatPrice(2500, 2, 6, TicketType.STUDENT));
    }

    @Test
    void vipRowCostsFiftyPercentMore() {
        assertEquals(3750, PriceCalculator.seatPrice(2500, 6, 6, TicketType.ADULT));
    }

    @Test
    void priceIsRoundedToWholeTenge() {
        assertEquals(1667, PriceCalculator.seatPrice(2381, 1, 6, TicketType.STUDENT));
    }
}