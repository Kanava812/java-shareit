package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> bookItem(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                           @Valid @RequestBody BookItemRequestDto bookingDto) {
        log.debug("Создание букинга ({}) пользователем с ID {}.", bookingDto,
                userId);
        LocalDateTime now = LocalDateTime.now().minusSeconds(5);
        if (bookingDto.getStart().equals(bookingDto.getEnd()) ||
                bookingDto.getStart().isBefore(now) ||
                bookingDto.getEnd().isBefore(now) ||
                bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new ValidationException("Недопустимые даты начала и окончания букинга.");
        }

        return bookingClient.bookItem(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approvingOfBooking(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                     @PathVariable @Positive Long bookingId,
                                                     @RequestParam Boolean approved) {
        log.debug("Подтверждение букинга ({}) с ID {} пользователем c ID{}",
                approved, bookingId, userId);
        return bookingClient.approvingOfBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                             @PathVariable @Positive Long bookingId) {
        log.debug("Просмотр букинга с ID {} пользователем с ID {}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                              @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                              @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.debug("Получение списка букинга, созданного пользователем с ID {} в зависимости от состояния: {}.", userId,
                stateParam);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsForAllItemsOfOwner(@RequestHeader("X-Sharer-User-Id") @Positive Long ownerId,
                                                                @RequestParam(name = "state", defaultValue = "ALL") String stateParam) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Неизвестное состояние: " + stateParam));
        log.info("Get booking with state {}, ownerId={}", stateParam, ownerId);
        return bookingClient.getBookingsForAllItemsOfOwner(ownerId, state);
    }
}
