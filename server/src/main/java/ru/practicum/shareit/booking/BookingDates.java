package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

public interface BookingDates {
    Long getItemId();

    LocalDateTime getStart();

    LocalDateTime getEnd();
}
