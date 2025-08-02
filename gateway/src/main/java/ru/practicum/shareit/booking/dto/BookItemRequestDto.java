package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {
	@Positive
	private Long itemId;

	@NotNull(message = "Необходимо задать начало периода бронирования")
	@FutureOrPresent
	private LocalDateTime start;

	@NotNull(message = "Необходимо задать конец периода бронирования")
	@Future
	private LocalDateTime end;
}
