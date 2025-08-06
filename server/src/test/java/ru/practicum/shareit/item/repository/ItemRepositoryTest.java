package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User testUser;
    private Item testItem;
    private ItemRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("name")
                .email("mail@ya.ru")
                .build();

        userRepository.save(testUser);

        testRequest = itemRequestRepository.save(ItemRequest.builder()
                .description("description")
                .requestor(testUser)
                .created(LocalDateTime.now())
                .build());

        testItem = Item.builder()
                .name("name")
                .description("description")
                .available(true)
                .owner(testUser)
                .request(testRequest)
                .build();

        itemRepository.save(testItem);
    }

    @Test
    void findItemByOwnerIdTest() {
        User newUser = userRepository.save(User.builder()
                .name("name")
                .email("mail1@ya.ru")
                .build());

        Item newItem = itemRepository.save(Item.builder()
                .name("name")
                .description("description")
                .available(true)
                .owner(newUser)
                .build());

        List<Item> itemsFromDb = itemRepository.findByOwnerId(newUser.getId());

        assertThat(itemsFromDb)
                .hasSize(1)
                .first()
                .usingRecursiveComparison()
                .isEqualTo(newItem);
    }

    @Test
    void findAvailableItemsByTextTest() {
        Item availableItem1 = itemRepository.save(Item.builder()
                .name("Предмет 1")
                .description("Описание предмета 1")
                .available(true)
                .owner(testUser)
                .build());

        Item availableItem2 = itemRepository.save(Item.builder()
                .name("Предмет 2")
                .description("Подробное описание предмета 2")
                .available(true)
                .owner(testUser)
                .build());

        Item unavailableItem = itemRepository.save(Item.builder()
                .name("Предмет 1")
                .description("Другой")
                .available(false)
                .owner(testUser)
                .build());

        List<Item> foundByName = itemRepository.search("Предмет 1");
        assertThat(foundByName)
                .hasSize(1)
                .containsExactly(availableItem1);

        List<Item> foundByDescription = itemRepository.search("подробное");
        assertThat(foundByDescription)
                .hasSize(1)
                .containsExactly(availableItem2);

        List<Item> foundCaseInsensitive = itemRepository.search("ПРЕДМЕТ 1");
        assertThat(foundCaseInsensitive)
                .hasSize(1)
                .containsExactly(availableItem1);

        List<Item> notFoundUnavailable = itemRepository.search("другой");
        assertThat(notFoundUnavailable).isEmpty();
    }

    @Test
    void findItemsByRequestIdTest() {
        ItemRequest request = itemRequestRepository.save(ItemRequest.builder()
                .description("Request")
                .requestor(testUser)
                .created(LocalDateTime.now())
                .build());

        Item itemWithRequest1 = itemRepository.save(Item.builder()
                .name("Item1 for Request")
                .description("Description 1")
                .available(true)
                .owner(testUser)
                .request(request)
                .build());

        Item itemWithRequest2 = itemRepository.save(Item.builder()
                .name("Item2 for Request")
                .description("Description 2")
                .available(true)
                .owner(testUser)
                .request(request)
                .build());

        List<Item> foundItems = itemRepository.findByRequestId(
                request.getId()
        );

        assertThat(foundItems)
                .hasSize(2)
                .containsExactlyInAnyOrder(itemWithRequest1, itemWithRequest2);
    }

    @Test
    void findItemsBySingleRequestIdTest() {
        ItemRequest newRequest = itemRequestRepository.save(ItemRequest.builder()
                .description("New request")
                .requestor(testUser)
                .created(LocalDateTime.now())
                .build());

        Item itemForRequest = itemRepository.save(Item.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .owner(testUser)
                .request(newRequest)
                .build());

        List<Item> foundItems = itemRepository.findByRequestId(newRequest.getId());

        assertThat(foundItems)
                .hasSize(1)
                .containsExactly(itemForRequest);
    }
}