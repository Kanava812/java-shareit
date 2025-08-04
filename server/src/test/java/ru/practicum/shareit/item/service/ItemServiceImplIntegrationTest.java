package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(ItemServiceImpl.class)
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemService itemService;

    private User testUser;
    private ItemRequest testRequest;
    private Item testItem;

    @BeforeEach
    void setUp() {

        testUser = userRepository.save(User.builder()
                .name("Test User")
                .email("test@email.com")
                .build());

        testRequest = itemRequestRepository.save(ItemRequest.builder()
                .description("test text")
                .requestor(testUser)
                .created(LocalDateTime.now())
                .build());

        testItem = itemRepository.save(Item.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(testUser)
                .build());
    }

    @Test
    @Transactional
    void addNewItemTest() {
        CreateItemDto createItemDto = CreateItemDto.builder()
                .name("Name")
                .description("Description")
                .available(true)
                .requestId(testRequest.getId())
                .build();

        ItemDto result = itemService.addNewItem(testUser.getId(), createItemDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Name");
        assertThat(result.getDescription()).isEqualTo("Description");
        assertThat(result.getAvailable()).isTrue();


        Item savedItem = itemRepository.findById(result.getId()).orElseThrow();
        assertThat(savedItem.getOwner().getId()).isEqualTo(testUser.getId());
        assertThat(savedItem.getRequest().getId()).isEqualTo(testRequest.getId());
    }

    @Test
    @Transactional
    void updateItemTest() {
        UpdateItemDto updateDto = UpdateItemDto.builder()
                .id(testItem.getId())
                .name("Update Name")
                .description("New")
                .available(false)
                .build();

        ItemDto result = itemService.updateItem(testUser.getId(), updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testItem.getId());
        assertThat(result.getName()).isEqualTo("Update Name");
        assertThat(result.getDescription()).isEqualTo("New");
        assertThat(result.getAvailable()).isFalse();

        Item updatedItem = itemRepository.findById(testItem.getId()).orElseThrow();
        assertThat(updatedItem.getName()).isEqualTo("Update Name");
        assertThat(updatedItem.getDescription()).isEqualTo("New");
        assertThat(updatedItem.getAvailable()).isFalse();
    }

    @Test
    void getSingleItemTest() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        Booking pastBooking = Booking.builder()
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .item(testItem)
                .booker(testUser)
                .status(BookingStatus.APPROVED)
                .build();

        Booking futureBooking = Booking.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .item(testItem)
                .booker(testUser)
                .status(BookingStatus.APPROVED)
                .build();

        bookingRepository.saveAll(List.of(pastBooking, futureBooking));

        Comment comment = Comment.builder()
                .text("Text")
                .item(testItem)
                .author(testUser)
                .created(now.minusHours(3))
                .build();

        commentRepository.save(comment);

        ItemWithBookingDto result = itemService.getItem(testUser.getId(), testItem.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testItem.getId());
        assertThat(result.getName()).isEqualTo(testItem.getName());
        assertThat(result.getDescription()).isEqualTo(testItem.getDescription());
        assertThat(result.getAvailable()).isEqualTo(testItem.getAvailable());

        assertThat(result.getLastBooking()).isEqualTo(now.minusDays(1));
        assertThat(result.getNextBooking()).isEqualTo(now.plusDays(1));

        assertThat(result.getComments())
                .hasSize(1)
                .first()
                .satisfies(c -> {
                    assertThat(c.getText()).isEqualTo("Text");
                    assertThat(c.getAuthorName()).isEqualTo(testUser.getName());
                    assertThat(c.getCreated()).isCloseTo(now.minusHours(3), within(1, ChronoUnit.SECONDS));
                });
    }

    @Test
    void getUsersItemsTest() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Item secondItem = itemRepository.save(Item.builder()
                .name("Name")
                .description("Description")
                .available(true)
                .owner(testUser)
                .build());

        Booking pastBooking = bookingRepository.save(Booking.builder()
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .item(testItem)
                .booker(testUser)
                .status(BookingStatus.APPROVED)
                .build());

        Booking futureBooking = bookingRepository.save(Booking.builder()
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .item(testItem)
                .booker(testUser)
                .status(BookingStatus.APPROVED)
                .build());

        Comment comment1 = commentRepository.save(Comment.builder()
                .text("Text")
                .item(testItem)
                .author(testUser)
                .created(now.minusHours(3))
                .build());

        Comment comment2 = commentRepository.save(Comment.builder()
                .text("Text2")
                .item(secondItem)
                .author(testUser)
                .created(now.minusHours(1))
                .build());

        List<ItemWithBookingDto> result = itemService.getItems(testUser.getId());

        assertThat(result).hasSize(2);

        // Проверяем первый предмет (с бронированиями и комментарием)
        assertThat(result)
                .filteredOn(i -> i.getId().equals(testItem.getId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getLastBooking()).isCloseTo(now.minusDays(1), within(1, ChronoUnit.SECONDS));
                    assertThat(item.getNextBooking()).isCloseTo(now.plusDays(1), within(1, ChronoUnit.SECONDS));
                    assertThat(item.getComments())
                            .hasSize(1)
                            .extracting(CommentDto::getText)
                            .containsExactly("Text");
                });

        // Проверяем второй предмет (только с комментарием)
        assertThat(result)
                .filteredOn(i -> i.getId().equals(secondItem.getId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getLastBooking()).isNull();
                    assertThat(item.getNextBooking()).isNull();
                    assertThat(item.getComments())
                            .hasSize(1)
                            .extracting(CommentDto::getText)
                            .containsExactly("Text2");
                });
    }

    @Test
    void searchItemsTest() {
        Item item1 = itemRepository.save(Item.builder()
                .name("AAA")
                .description("GGG")
                .available(true)
                .owner(testUser)
                .build());

        Item item2 = itemRepository.save(Item.builder()
                .name("BBB")
                .description("DDD")
                .available(true)
                .owner(testUser)
                .build());

        List<ItemDto> result1 = itemService.findItems(testUser.getId(), "A");
        assertThat(result1)
                .hasSize(1)
                .extracting(ItemDto::getName)
                .containsExactly("AAA");

        List<ItemDto> result2 = itemService.findItems(testUser.getId(), "DD");
        assertThat(result2)
                .hasSize(1)
                .extracting(ItemDto::getName)
                .containsExactly("BBB");

        List<ItemDto> result3 = itemService.findItems(testUser.getId(), "CCC");
        assertThat(result3).isEmpty();
    }

    @Test
    @Transactional
    void addCommentTest() {
        User booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@ya.ru")
                .build());

        Booking pastBooking = bookingRepository.save(Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(testItem)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        CreateCommentDto commentDto = CreateCommentDto.builder()
                .text("Text")
                .build();

        CommentDto result = itemService.addComment(booker.getId(), testItem.getId(), commentDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getText()).isEqualTo("Text");
        assertThat(result.getAuthorName()).isEqualTo(booker.getName());
        assertThat(result.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());

        Comment savedComment = commentRepository.findById(result.getId()).orElseThrow();
        assertThat(savedComment.getItem().getId()).isEqualTo(testItem.getId());
        assertThat(savedComment.getAuthor().getId()).isEqualTo(booker.getId());
        assertThat(savedComment.getText()).isEqualTo("Text");
    }
}


