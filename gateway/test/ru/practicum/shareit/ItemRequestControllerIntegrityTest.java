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
import ru.practicum.shareit.exception.ExceptionController;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@Import({ExceptionController.class})
class ItemRequestControllerIntegrityTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestClient requestClient;

    @SneakyThrows
    @Test
    void addItemRequestValidTest() {
        CreateItemRequestDto requestDto = new CreateItemRequestDto("request");

        when(requestClient.addNewItemRequest(anyLong(), any(CreateItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(requestClient).addNewItemRequest(eq(1L), argThat(dto ->
                dto.getDescription().equals("request")
        ));
    }

    @SneakyThrows
    @Test
    void addItemRequestEmptyDescriptionTest() {
        CreateItemRequestDto emptyRequest = new CreateItemRequestDto("");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).addNewItemRequest(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void addItemRequestBlankDescriptionTest() {
        CreateItemRequestDto blankDescription = new CreateItemRequestDto(" ");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(blankDescription)))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).addNewItemRequest(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void addItemRequestInvalidUserIdTest() {
        CreateItemRequestDto validDto = new CreateItemRequestDto("Valid description");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(requestClient, never()).addNewItemRequest(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void getUserRequestsTest() {
        when(requestClient.getItemRequestsByRequestor(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(requestClient).getItemRequestsByRequestor(1L);
    }

    @SneakyThrows
    @Test
    void getAllRequestsTest() {
        when(requestClient.getAllItemRequests(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(requestClient).getAllItemRequests(1L);
    }

    @SneakyThrows
    @Test
    void getUserRequestByIdValidTest() {
        when(requestClient.getItemRequestById(anyLong(),anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk());

        verify(requestClient).getItemRequestById(1L, 1L);
    }

    @SneakyThrows
    @Test
    void getUserRequestByIdInvalidTest() {
        mockMvc.perform(get("/requests/0") // неверный requestId
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(requestClient, never()).getItemRequestById(anyLong(),anyLong());
    }
}