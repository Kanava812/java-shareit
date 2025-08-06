package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private ItemRequest itemRequest;
    private CreateItemRequestDto createDto;
    private ItemRequestDto itemRequestDto;
    private ItemForRequestDto itemForRequestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@ya.ru")
                .build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Text")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        createDto = CreateItemRequestDto.builder()
                .description("Text")
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Text")
                .created(itemRequest.getCreated())
                .build();

        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(user)
                .request(itemRequest)
                .build();

        itemForRequestDto = ItemForRequestDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(user.getId())
                .build();
    }

    @Test
    void addItemRequestValidReturnsSavedRequestTest() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto result = itemRequestService.addNewItemRequest(user.getId(), createDto);

        assertThat(result.getId()).isEqualTo(itemRequestDto.getId());
        assertThat(result.getDescription()).isEqualTo(itemRequestDto.getDescription());
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void addItemRequestUserNotFoundThrowExceptionTest() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.addNewItemRequest(100L, createDto))
                .isInstanceOf(EntityNotFoundException.class);

        verify(itemRequestRepository, never()).save(any());
    }
}


