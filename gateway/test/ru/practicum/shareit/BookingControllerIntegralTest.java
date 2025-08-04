package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ExceptionHandler;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@Import({ExceptionHandler.class})
class BookingControllerIntegralTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @SneakyThrows
    @Test
    void bookItemValidRequestTest() {
        BookItemRequestDto requestDto = new BookItemRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        when(bookingClient.bookItem(anyLong(), any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(bookingClient).bookItem(eq(1L), argThat(dto ->
                dto.getItemId() == 1L &&
                        dto.getStart().isAfter(LocalDateTime.now()) &&
                        dto.getEnd().isAfter(dto.getStart())
        ));
    }

    @SneakyThrows
    @Test
    void bookItemInvalidDatesTest() {
        // start в прошлом
        BookItemRequestDto pastStart = new BookItemRequestDto(
                1L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        // end не в будущем
        BookItemRequestDto pastEnd = new BookItemRequestDto(
                1L,
                LocalDateTime.now(),
                LocalDateTime.now().minusDays(1)
        );

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(pastStart)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(pastEnd)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void approveBookingValidTest() {
        when(bookingClient.approvingOfBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(bookingClient).approvingOfBooking(1L, 1L, true);
    }

    @SneakyThrows
    @Test
    void approveBookingInvalidIdTest() {
        mockMvc.perform(patch("/bookings/0?approved=true")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(bookingClient, never()).approvingOfBooking(anyLong(), anyLong(), anyBoolean());
    }

    @SneakyThrows
    @Test
    void getBookingValidTest() {
        when(bookingClient.getBooking(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(bookingClient).getBooking(1L, 1L);
    }

    @SneakyThrows
    @Test
    void getBookingInvalidIdTest() {
        mockMvc.perform(get("/bookings/0")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(bookingClient, never()).getBooking(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void getBookingsValidTest() {
        when(bookingClient.getBookings(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings?state=all")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(bookingClient).getBookings(1L, BookingState.ALL, 0, 10);
    }

    @SneakyThrows
    @Test
    void getBookingsInvalidStateTest() {
        mockMvc.perform(get("/bookings?state=invalid")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(bookingClient, never()).getBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @SneakyThrows
    @Test
    void getBookingsByOwnerValidTest() {
        when(bookingClient.getBookingsForAllItemsOfOwner(anyLong(), any(BookingState.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner?state=all")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(bookingClient).getBookingsForAllItemsOfOwner(1L, BookingState.ALL);
    }

    @SneakyThrows
    @Test
    void getBookingsByOwnerInvalidStateTest() {
        mockMvc.perform(get("/bookings/owner?state=invalid")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(bookingClient, never()).getBookingsForAllItemsOfOwner(anyLong(), any());
    }
}