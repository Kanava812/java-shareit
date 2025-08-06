package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody CreateBookingDto bookingDto) {
        log.debug("Создание букинга ({}) пользователем с ID {}.", bookingDto,
                userId);
        return bookingService.addBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approvingOfBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long bookingId,
                                         @RequestParam Boolean approved) {
        log.debug("Подтверждение букинга ({}) с ID {} пользователем c ID{}",
                approved, bookingId, userId);
        return bookingService.approvingOfBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId) {
        log.debug("Просмотр букинга с ID {} пользователем с ID {}", bookingId, userId);
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookingsByUserAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @RequestParam(defaultValue = "ALL") BookingState state) {
        log.debug("Получение списка букинга, созданного пользователем с ID {} в зависимости от состояния: {}.", userId,
                state);
        return bookingService.getBookingsByUserAndState(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsForAllItemsOfOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                          @RequestParam(defaultValue = "ALL") BookingState state) {
        log.debug("Получение списка букинга на вещи пользователя с ID {} в зависимости от состояния: {}", userId, state);
        return bookingService.getBookingsForAllItemsOfOwner(userId, state);
    }
}
