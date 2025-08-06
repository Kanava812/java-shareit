package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithAnswers;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(ItemRequestServiceImpl.class)
class ItemRequestServiceImplIntegralTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestService itemRequestService;

    private User user;
    private User otherUser;
    private ItemRequest itemRequest;
    private Item item;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .name("User")
                .email("user@ya.ru")
                .build());

        otherUser = userRepository.save(User.builder()
                .name("Other User")
                .email("other@ya.ru")
                .build());

        itemRequest = itemRequestRepository.save(ItemRequest.builder()
                .description("Text")
                .requestor(user)
                .created(LocalDateTime.now())
                .build());

        item = itemRepository.save(Item.builder()
                .name("Item")
                .description("description")
                .available(true)
                .owner(otherUser)
                .request(itemRequest)
                .build());
    }

    @Test
    @Transactional
    void addItemRequestSaveNewRequestTest() {
        CreateItemRequestDto dto = CreateItemRequestDto.builder()
                .description("request")
                .build();

        ItemRequestDto result = itemRequestService.addNewItemRequest(user.getId(), dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("request");
        assertThat(itemRequestRepository.findAll()).hasSize(2);
    }

    @Test
    @Transactional
    void addItemRequestUserNotFoundThrowExceptionTest() {
        CreateItemRequestDto dto = CreateItemRequestDto.builder()
                .description("request")
                .build();

        assertThatThrownBy(() -> itemRequestService.addNewItemRequest(100L, dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getUserRequestsReturnRequestsWithItemsTest() {
        List<ItemRequestDtoWithAnswers> result = itemRequestService.getItemRequestsByRequestor(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getItems()).hasSize(1);
        assertThat(result.getFirst().getItems().getFirst().getName()).isEqualTo("Item");
    }

    @Test
    void getUserRequestsNoRequestsReturnEmptyListTest() {
        itemRequestRepository.deleteAll();
        item.setRequest(null);
        itemRepository.save(item);

        List<ItemRequestDtoWithAnswers> result = itemRequestService.getItemRequestsByRequestor(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getAllRequestsExceptUserReturnOtherUsersRequestsTest() {
        List<ItemRequestDtoWithAnswers> result = itemRequestService.getAllItemRequests(otherUser.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Text");
    }
}