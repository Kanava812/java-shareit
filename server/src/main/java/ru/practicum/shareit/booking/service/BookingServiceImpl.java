package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.exception.AccessNotAllowedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto addBooking(Long userId, CreateBookingDto bookingDto) {
        log.debug("Поиск пользователя с ID {}.", userId);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден"));
        log.debug("Поиск предмета для букинга с ID {}.", bookingDto.getItemId());
        Item item = itemRepository.findById(bookingDto.getItemId()).orElseThrow(
                () -> new EntityNotFoundException("Предмет не найден"));
        log.debug("Проверка доступности предмета.");
        if (!item.getAvailable()) {
            throw new ValidationException("Предмет не доступен для букинга.");
        }
        log.debug("Проверка дат начала и окончания букинга.");

        Booking booking = BookingMapper.toBookingCreate(bookingDto);
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        log.debug("Создание букинга");
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto approvingOfBooking(Long userId, Long bookingId, Boolean approved) {
        log.debug("Проверка прав пользователя с ID {} на установку статуса букинга предмета.", userId);
        Booking booking = getBookingById(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessNotAllowedException("Пользователь не является владельцем и не может менять статус");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new AccessNotAllowedException("Для изменения статус букинга предмета должен быть WAITING");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBooking(Long userId, Long bookingId) {
        log.debug("Проверка прав пользователя с ID {} на просмотр букинга предмета.", userId);
        Booking booking = getBookingById(bookingId);
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessNotAllowedException("Пользователь не является ни владельцем вещи, " +
                    "ни автором букинга, поэтому просмотр запрещен.");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByUserAndState(Long userId, BookingState state) {
        log.debug("Поиск пользователя с ID {}.", userId);
        if (!userRepository.existsById(userId)) {
                throw  new EntityNotFoundException("Пользователь не найден");
        }
        log.debug("Список букингов в зависимости от запрошенного состояния: {}", state);
        List<Booking> bookings = switch (state) {
            case BookingState.ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case BookingState.CURRENT -> bookingRepository.findAllByBookerIdAndCurrentTime(userId, LocalDateTime.now());
            case BookingState.PAST ->
                    bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
            case BookingState.FUTURE ->
                    bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
            case BookingState.WAITING ->
                    bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case BookingState.REJECTED ->
                    bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        };
        log.debug("Найдено {} букингов: {}.", bookings.size(), bookings);
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsForAllItemsOfOwner(Long ownerId, BookingState state) {
        log.debug("Поиск пользователя с ID {}.", ownerId);
        if (!userRepository.existsById(ownerId)) {
            throw  new EntityNotFoundException("Пользователь не найден");
        }
        log.debug("Получение список букингов предмета в зависимости от запрошенного состояния: {}", state);
        List<Booking> bookings = switch (state) {
            case BookingState.ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId);
            case BookingState.CURRENT -> bookingRepository.findAllByOwnerIdAndCurrentTime(ownerId, LocalDateTime.now());
            case BookingState.PAST ->
                    bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, LocalDateTime.now());
            case BookingState.FUTURE ->
                    bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, LocalDateTime.now());
            case BookingState.WAITING ->
                    bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
            case BookingState.REJECTED ->
                    bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
        };
        log.debug("Найдено {} букингов: {}.", bookings.size(), bookings);
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    private Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(
                () -> new EntityNotFoundException("Букинга с ID " + bookingId + " не найдено.")
        );
    }
}
