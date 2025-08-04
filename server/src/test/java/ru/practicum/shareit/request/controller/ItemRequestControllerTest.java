package ru.practicum.shareit.request.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithAnswers;
import ru.practicum.shareit.request.service.ItemRequestService;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    @Mock
    private ItemRequestService itemRequestService;

    @InjectMocks
    private ItemRequestController itemRequestController;

    private Long userId;
    private CreateItemRequestDto createRequestDto;
    private ItemRequestDto itemRequestDto;
    private ItemRequestDtoWithAnswers itemRequestDtoWithAnswers;
    private ItemForRequestDto itemForRequestDto;

    @BeforeEach
    void setUp() {
        userId = 1L;

        createRequestDto = CreateItemRequestDto.builder()
                .description("Text")
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Text")
                .created(LocalDateTime.now())
                .build();

        itemForRequestDto = ItemForRequestDto.builder()
                .id(1L)
                .name("Name")
                .ownerId(2L)
                .build();

        itemRequestDtoWithAnswers = ItemRequestDtoWithAnswers.builder()
                .id(1L)
                .description("Text")
                .created(LocalDateTime.now())
                .items(List.of(itemForRequestDto))
                .build();
    }

    @Test
    void addItemRequest_validRequest_createdSuccessfullyTest() {
        when(itemRequestService.addNewItemRequest(userId, createRequestDto)).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestController.addNewItemRequest(userId, createRequestDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Text");
        assertThat(result.getCreated()).isNotNull();
        verify(itemRequestService).addNewItemRequest(userId, createRequestDto);
    }

    @Test
    void getUserRequests_hasRequests_requestsReturnedCorrectlyTest() {
        List<ItemRequestDtoWithAnswers> expected = List.of(itemRequestDtoWithAnswers);
        when(itemRequestService.getItemRequestsByRequestor(userId)).thenReturn(expected);

        List<ItemRequestDtoWithAnswers> result = itemRequestController.getItemRequestsByRequestor(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getItems()).hasSize(1);
        assertThat(result.getFirst().getItems().getFirst().getName()).isEqualTo("Name");
        verify(itemRequestService).getItemRequestsByRequestor(userId);
    }

    @Test
    void getAllRequestsExceptUser_otherUsersHaveRequests_requestsReturnedTest() {
        List<ItemRequestDtoWithAnswers> expectedRequests = List.of(itemRequestDtoWithAnswers);
        when(itemRequestService.getItemRequestsByRequestor(userId)).thenReturn(expectedRequests);

        List<ItemRequestDtoWithAnswers> result = itemRequestController.getItemRequestsByRequestor(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Text");
        verify(itemRequestService).getItemRequestsByRequestor(userId);
    }

    @Test
    void getUserRequestById_requestExists_requestRetrievedTest() {
        Long requestId = 1L;
        when(itemRequestService.getItemRequestById(userId, requestId)).thenReturn(itemRequestDtoWithAnswers);

        ItemRequestDtoWithAnswers result = itemRequestController.getItemRequestById(userId, requestId);

        assertThat(result.getId()).isEqualTo(1L);
        verify(itemRequestService).getItemRequestById(userId, requestId);
    }
}