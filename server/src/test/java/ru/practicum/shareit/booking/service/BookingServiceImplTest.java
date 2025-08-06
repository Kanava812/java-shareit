package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.AccessNotAllowedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private CreateBookingDto createBookingDto;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@email.com")
                .build();

        booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("booker@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        createBookingDto = CreateBookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    void addingBookingTest() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.addBooking(booker.getId(), createBookingDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(booking.getId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void missingUserAddingBookingTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.addBooking(99L, createBookingDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void missingItemAddingBookingTest() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.addBooking(booker.getId(), createBookingDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Предмет не найден");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void unavailableItemAddingBookingTest() {
        item.setAvailable(false);

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.addBooking(booker.getId(), createBookingDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Предмет не доступен для букинга.");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approvingBookingTest() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.approvingOfBooking(owner.getId(), booking.getId(), true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void unauthorizedApprovingBookingTest() {
        Long notOwnerId = 99L;
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approvingOfBooking(notOwnerId, booking.getId(), true))
                .isInstanceOf(AccessNotAllowedException.class)
                .hasMessageContaining("Пользователь не является владельцем и не может менять статус");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void wrongStatusApprovingBookingTest() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approvingOfBooking(owner.getId(), booking.getId(), true))
                .isInstanceOf(AccessNotAllowedException.class)
                .hasMessageContaining("должен быть WAITING");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void rejectingBookingTest() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.approvingOfBooking(owner.getId(), booking.getId(), false);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void fetchingBookingTest() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBooking(booker.getId(), booking.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void unauthorizedFetchingBookingTest() {
        Long randomUserId = 99L;
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBooking(randomUserId, booking.getId()))
                .isInstanceOf(AccessNotAllowedException.class)
                .hasMessageContaining("Пользователь не является ни владельцем вещи, ни автором букинга, поэтому просмотр запрещен.");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void fetchingUserBookingsTest() {
        when(userRepository.existsById(booker.getId())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(booker.getId()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByUserAndState(booker.getId(), BookingState.ALL);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(booking.getId());
    }

    @Test
    void userNotFoundFetchingBookingsTest() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> bookingService.getBookingsByUserAndState(99L, BookingState.ALL))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void fetchingCurrentBookingsTest() {
        Booking currentBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        when(userRepository.existsById(booker.getId())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndCurrentTime(eq(booker.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of(currentBooking));

        List<BookingDto> result = bookingService.getBookingsByUserAndState(booker.getId(), BookingState.CURRENT);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(currentBooking.getId());
        verify(bookingRepository).findAllByBookerIdAndCurrentTime(eq(booker.getId()), any(LocalDateTime.class));
    }

    @Test
    void fetchingPastBookingsTest() {
        Booking pastBooking = Booking.builder()
                .id(3L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        when(userRepository.existsById(booker.getId())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of(pastBooking));

        List<BookingDto> result = bookingService.getBookingsByUserAndState(booker.getId(), BookingState.PAST);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(pastBooking.getId());
        verify(bookingRepository).findAllByBookerIdAndEndBeforeOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class));
    }

    @Test
    void fetchingFutureBookingsTest() {
        Booking futureBooking = Booking.builder()
                .id(4L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.existsById(booker.getId())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of(futureBooking));

        List<BookingDto> result = bookingService.getBookingsByUserAndState(booker.getId(), BookingState.FUTURE);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(futureBooking.getId());
        verify(bookingRepository).findAllByBookerIdAndStartAfterOrderByStartDesc(eq(booker.getId()), any(LocalDateTime.class));
    }

    @Test
    void fetchingWaitingBookingsTest() {
        Booking waitingBooking = Booking.builder()
                .id(5L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.existsById(booker.getId())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(eq(booker.getId()), eq(BookingStatus.WAITING)))
                .thenReturn(List.of(waitingBooking));

        List<BookingDto> result = bookingService.getBookingsByUserAndState(booker.getId(), BookingState.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(waitingBooking.getId());
        verify(bookingRepository).findAllByBookerIdAndStatusOrderByStartDesc(eq(booker.getId()), eq(BookingStatus.WAITING));
    }

    @Test
    void fetchingRejectedBookingsTest() {
        Booking rejectedBooking = Booking.builder()
                .id(6L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .build();

        when(userRepository.existsById(booker.getId())).thenReturn(true);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(eq(booker.getId()), eq(BookingStatus.REJECTED)))
                .thenReturn(List.of(rejectedBooking));

        List<BookingDto> result = bookingService.getBookingsByUserAndState(booker.getId(), BookingState.REJECTED);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(rejectedBooking.getId());
        verify(bookingRepository).findAllByBookerIdAndStatusOrderByStartDesc(eq(booker.getId()), eq(BookingStatus.REJECTED));
    }

    @Test
    void fetchingOwnerBookingsTest() {
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(owner.getId()))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsForAllItemsOfOwner(owner.getId(), BookingState.ALL);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(booking.getId());
    }

    @Test
    void ownerNotFoundFetchingBookingsTest() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> bookingService.getBookingsForAllItemsOfOwner(99L, BookingState.ALL))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void fetchingOwnerCurrentBookingsTest() {
        Booking currentBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(bookingRepository.findAllByOwnerIdAndCurrentTime(eq(owner.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of(currentBooking));

        List<BookingDto> result = bookingService.getBookingsForAllItemsOfOwner(owner.getId(), BookingState.CURRENT);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(currentBooking.getId());
        verify(bookingRepository).findAllByOwnerIdAndCurrentTime(eq(owner.getId()), any(LocalDateTime.class));
    }

    @Test
    void fetchingOwnerPastBookingsTest() {
        Booking pastBooking = Booking.builder()
                .id(3L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of(pastBooking));

        List<BookingDto> result = bookingService.getBookingsForAllItemsOfOwner(owner.getId(), BookingState.PAST);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(pastBooking.getId());
        verify(bookingRepository).findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class));
    }

    @Test
    void fetchFutureBookingsTest() {
        Booking futureBooking = Booking.builder()
                .id(4L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of(futureBooking));

        List<BookingDto> result = bookingService.getBookingsForAllItemsOfOwner(owner.getId(), BookingState.FUTURE);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(futureBooking.getId());
        verify(bookingRepository).findAllByItemOwnerIdAndStartAfterOrderByStartDesc(eq(owner.getId()), any(LocalDateTime.class));
    }

    @Test
    void fetchWaitingBookingsTest() {
        Booking waitingBooking = Booking.builder()
                .id(5L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), eq(BookingStatus.WAITING)))
                .thenReturn(List.of(waitingBooking));

        List<BookingDto> result = bookingService.getBookingsForAllItemsOfOwner(owner.getId(), BookingState.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(waitingBooking.getId());
        verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), eq(BookingStatus.WAITING));
    }

    @Test
    void fetchRejectedBookingsTest() {
        Booking rejectedBooking = Booking.builder()
                .id(6L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .build();

        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), eq(BookingStatus.REJECTED)))
                .thenReturn(List.of(rejectedBooking));

        List<BookingDto> result = bookingService.getBookingsForAllItemsOfOwner(owner.getId(), BookingState.REJECTED);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(rejectedBooking.getId());
        verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(eq(owner.getId()), eq(BookingStatus.REJECTED));
    }
}