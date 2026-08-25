package de.fitnesscoach.data.db;

import static org.junit.Assert.*;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.Test;
import de.fitnesscoach.data.entity.DomainEnums;

public class RoomConvertersTest {
    @Test public void instantRoundTripIsDeterministic() {
        Instant value = Instant.parse("2026-08-25T12:00:00Z");
        assertEquals(value, RoomConverters.toInstant(RoomConverters.fromInstant(value)));
    }
    @Test public void localDateRoundTripIsDeterministic() {
        LocalDate value = LocalDate.of(2026, 8, 25);
        assertEquals(value, RoomConverters.toLocalDate(RoomConverters.fromLocalDate(value)));
    }
    @Test public void nullValuesStayNull() {
        assertNull(RoomConverters.fromInstant(null));
        assertNull(RoomConverters.toLocalDate(null));
    }
    @Test public void enumRoundTripWorks() {
        assertEquals(DomainEnums.DataQuality.MISSING,
                RoomConverters.toDataQuality(RoomConverters.fromDataQuality(DomainEnums.DataQuality.MISSING)));
    }
}