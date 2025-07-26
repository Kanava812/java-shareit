package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingDto {
    @NotNull(message = "Id обязателен.")
    private Long itemId;

    @NotNull(message = "Дата начала букинга обязательна.")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания букинга обязательна.")
    private LocalDateTime end;
}
