package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingDates;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    ItemServiceImpl itemService;

    @Test
    void addNewItemTest() {
        Long userId = 1L;
        CreateItemDto createItemDto = CreateItemDto.builder()
                .name("Name")
                .description("Description")
                .available(true)
                .build();

        User owner = User.builder()
                .id(userId)
                .name("Owner")
                .build();

        Item savedItem = Item.builder()
                .id(1L)
                .name("Name")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.addNewItem(userId, createItemDto);


        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Name");
        verify(itemRepository).save(argThat(item ->
                item.getName().equals("Name") &&
                        item.getOwner().equals(owner)
        ));
    }

    @Test
    void addNewItemUserNotFoundTest() {
        Long userId = 999L;
        CreateItemDto createItemDto = new CreateItemDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addNewItem(userId, createItemDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(itemRepository, never()).save(any());
    }

    @Test
    void addNewItemRequestNotFoundTest() {
        Long userId = 1L;
        Long nonExistentRequestId = 999L;

        CreateItemDto createItemDto = CreateItemDto.builder()
                .name("Name")
                .description("Description")
                .available(true)
                .requestId(nonExistentRequestId)
                .build();

        User owner = User.builder()
                .id(userId)
                .name("Owner")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(nonExistentRequestId)).thenReturn(Optional.empty());

        // Проверка
        assertThatThrownBy(() -> itemService.addNewItem(userId, createItemDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Запрос не найден");

        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItemTest() {
        Long userId = 1L;
        Long itemId = 10L;

        User owner = User.builder()
                .id(userId)
                .build();

        Item existingItem = Item.builder()
                .id(itemId)
                .name("Name")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        UpdateItemDto updateDto = UpdateItemDto.builder()
                .id(itemId)
                .name("Update Name")
                .build();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto result = itemService.updateItem(userId, updateDto);

        assertThat(result.getName()).isEqualTo("Update Name");
        assertThat(result.getDescription()).isEqualTo("Description");
        verify(itemRepository).save(argThat(item ->
                item.getName().equals("Update Name") &&
                        item.getDescription().equals("Description")
        ));

        verify(userRepository).existsById(userId);
    }

    @Test
    void updateItemItemIdNullTest() {
        Long userId = 1L;

        UpdateItemDto updateDto = UpdateItemDto.builder().name("Update Name")
                .description("Update Description")
                .available(false)
                .build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findById(null)).thenReturn(Optional.empty());
        // Ждём, что исключение будет брошено
        assertThatThrownBy(() -> itemService.updateItem(userId, updateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Вещь не найдена");

        // Метода save не должно быть вызвано, так как предмет не найден
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItemItemNotFoundTest() {
        Long userId = 1L;
        Long nonExistentItemId = 999L;

        UpdateItemDto updateDto = UpdateItemDto.builder()
                .id(nonExistentItemId)
                .name("Update Name")
                .build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findById(nonExistentItemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateItem(userId, updateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Вещь не найдена");

        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItemNotOwnerTest() {
        Long ownerId = 1L;
        Long otherUserId = 2L;
        Long itemId = 10L;

        User owner = User.builder().id(ownerId).build();
        Item existingItem = Item.builder()
                .id(itemId)
                .owner(owner)
                .build();

        UpdateItemDto updateDto = UpdateItemDto.builder()
                .id(itemId)
                .name("Update Name")
                .build();

        when(userRepository.existsById(otherUserId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        assertThatThrownBy(() -> itemService.updateItem(otherUserId, updateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Обновление отклонено. Пользователь с ID 2 не является владельцем предмета:");

        verify(userRepository).existsById(otherUserId);
        verify(itemRepository).findById(itemId);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getSingleItemTest() {
        // Given
        Long ownerId = 1L;
        Long itemId = 10L;
        LocalDateTime now = LocalDateTime.now();

        User owner = User.builder()
                .id(ownerId)
                .name("Owner")
                .build();

        Item item = Item.builder()
                .id(itemId)
                .name("Name")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        LocalDateTime lastBookingEnd = now.minusDays(1);
        LocalDateTime nextBookingStart = now.plusDays(1);

        when(bookingRepository.findAllBookingsByItemId(itemId))
                .thenReturn(List.of(
                        new BookingDates() {
                            public Long getItemId() {
                                return itemId;
                            }

                            public LocalDateTime getStart() {
                                return now.minusDays(2);
                            }

                            public LocalDateTime getEnd() {
                                return lastBookingEnd;
                            }
                        },
                        new BookingDates() {
                            public Long getItemId() {
                                return itemId;
                            }

                            public LocalDateTime getStart() {
                                return nextBookingStart;
                            }

                            public LocalDateTime getEnd() {
                                return now.plusDays(2);
                            }
                        }
                ));

        Comment comment = Comment.builder()
                .id(1L)
                .text("Text")
                .author(owner)
                .created(now.minusHours(3))
                .build();

        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Text")
                .authorName("Owner")
                .created(now.minusHours(3))
                .build();

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemIdOrderByCreatedDesc(itemId)).thenReturn(List.of(comment));

        ItemWithBookingDto result = itemService.getItem(ownerId, itemId);


        assertThat(result.getId()).isEqualTo(itemId);
        assertThat(result.getLastBooking()).isEqualTo(lastBookingEnd);
        assertThat(result.getNextBooking()).isEqualTo(nextBookingStart);

        assertThat(result.getComments())
                .hasSize(1)
                .first()
                .satisfies(c -> {
                    assertThat(c.getId()).isEqualTo(1L);
                    assertThat(c.getText()).isEqualTo("Text");
                    assertThat(c.getAuthorName()).isEqualTo("Owner");
                    assertThat(c.getCreated()).isBefore(now);
                });
    }

    @Test
    void getItemItemNotFoundTest() {
        Long userId = 1L;
        Long nonExistentItemId = 999L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findById(nonExistentItemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItem(userId, nonExistentItemId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Предмет не найден");

        verify(bookingRepository, never()).findAllBookingsByItemId(any());
        verify(commentRepository, never()).findAllByItemIdOrderByCreatedDesc(any());
    }

    @Test
    void getItemWithPastAndFutureBookingsTest() {
        Long ownerId = 1L;
        Long itemId = 1L;
        LocalDateTime now = LocalDateTime.now();

        User owner = User.builder().id(ownerId).build();
        Item item = Item.builder().id(itemId).owner(owner).build();

        BookingDates pastBooking = new BookingDates() {
            public Long getItemId() {
                return itemId;
            }

            public LocalDateTime getStart() {
                return now.minusDays(2);
            }

            public LocalDateTime getEnd() {
                return now.minusDays(1);
            }
        };

        BookingDates futureBooking = new BookingDates() {
            public Long getItemId() {
                return itemId;
            }

            public LocalDateTime getStart() {
                return now.plusDays(1);
            }

            public LocalDateTime getEnd() {
                return now.plusDays(2);
            }
        };

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllBookingsByItemId(itemId))
                .thenReturn(List.of(pastBooking, futureBooking));
        when(commentRepository.findAllByItemIdOrderByCreatedDesc(itemId)).thenReturn(Collections.emptyList());

        ItemWithBookingDto result = itemService.getItem(ownerId, itemId);

        assertThat(result.getLastBooking()).isEqualTo(now.minusDays(1));
        assertThat(result.getNextBooking()).isEqualTo(now.plusDays(1));
    }

    @Test
    void getItemWithOnlyPastBookingsTest() {

        Long ownerId = 1L;
        Long itemId = 1L;
        LocalDateTime now = LocalDateTime.now();

        User owner = User.builder().id(ownerId).build();
        Item item = Item.builder().id(itemId).owner(owner).build();

        BookingDates pastBooking1 = new BookingDates() {
            public Long getItemId() {
                return itemId;
            }

            public LocalDateTime getStart() {
                return now.minusDays(3);
            }

            public LocalDateTime getEnd() {
                return now.minusDays(2);
            }
        };

        BookingDates pastBooking2 = new BookingDates() {
            public Long getItemId() {
                return itemId;
            }

            public LocalDateTime getStart() {
                return now.minusDays(1);
            }

            public LocalDateTime getEnd() {
                return now.minusHours(1);
            }
        };

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllBookingsByItemId(itemId))
                .thenReturn(List.of(pastBooking1, pastBooking2));
        when(commentRepository.findAllByItemIdOrderByCreatedDesc(itemId)).thenReturn(Collections.emptyList());

        ItemWithBookingDto result = itemService.getItem(ownerId, itemId);

        assertThat(result.getLastBooking()).isEqualTo(now.minusHours(1));
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void getItemWithOnlyFutureBookingsTest() {

        Long ownerId = 1L;
        Long itemId = 1L;
        LocalDateTime now = LocalDateTime.now();

        User owner = User.builder().id(ownerId).build();
        Item item = Item.builder().id(itemId).owner(owner).build();

        BookingDates futureBooking1 = new BookingDates() {
            public Long getItemId() {
                return itemId;
            }

            public LocalDateTime getStart() {
                return now.plusDays(1);
            }

            public LocalDateTime getEnd() {
                return now.plusDays(2);
            }
        };

        BookingDates futureBooking2 = new BookingDates() {
            public Long getItemId() {
                return itemId;
            }

            public LocalDateTime getStart() {
                return now.plusDays(3);
            }

            public LocalDateTime getEnd() {
                return now.plusDays(4);
            }
        };

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllBookingsByItemId(itemId))
                .thenReturn(List.of(futureBooking1, futureBooking2));
        when(commentRepository.findAllByItemIdOrderByCreatedDesc(itemId)).thenReturn(Collections.emptyList());

        ItemWithBookingDto result = itemService.getItem(ownerId, itemId);

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isEqualTo(now.plusDays(1));
    }

    @Test
    void getItemWithCurrentBookingTest() {

        Long ownerId = 1L;
        Long itemId = 1L;
        LocalDateTime now = LocalDateTime.now();

        User owner = User.builder().id(ownerId).build();
        Item item = Item.builder().id(itemId).owner(owner).build();

        BookingDates currentBooking = new BookingDates() {
            public Long getItemId() {
                return itemId;
            }

            public LocalDateTime getStart() {
                return now.minusDays(1);
            }

            public LocalDateTime getEnd() {
                return now.plusDays(1);
            }
        };

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findAllBookingsByItemId(itemId))
                .thenReturn(List.of(currentBooking));
        when(commentRepository.findAllByItemIdOrderByCreatedDesc(itemId)).thenReturn(Collections.emptyList());

        ItemWithBookingDto result = itemService.getItem(ownerId, itemId);

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void getItemNonOwnerTest() {

        Long ownerId = 1L;
        Long otherUserId = 2L;
        Long itemId = 1L;

        User owner = User.builder().id(ownerId).build();
        Item item = Item.builder().id(itemId).owner(owner).build();

        when(userRepository.existsById(otherUserId)).thenReturn(true);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemIdOrderByCreatedDesc(itemId)).thenReturn(Collections.emptyList());

        ItemWithBookingDto result = itemService.getItem(otherUserId, itemId);

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
        verify(bookingRepository, never()).findAllBookingsByItemId(any());
    }

    @Test
    void getUsersItemsTest() {
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();

        User owner = User.builder()
                .id(userId)
                .name("Owner")
                .email("owner@ya.ru")
                .build();

        Item item1 = Item.builder()
                .id(1L)
                .name("Name")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        Item item2 = Item.builder()
                .id(2L)
                .name("Name2")
                .description("Description2")
                .available(false)
                .owner(owner)
                .build();

        BookingDates pastBooking = new BookingDates() {
            public Long getItemId() {
                return 1L;
            }

            public LocalDateTime getStart() {
                return now.minusDays(2);
            }

            public LocalDateTime getEnd() {
                return now.minusDays(1);
            }
        };

        BookingDates futureBooking = new BookingDates() {
            public Long getItemId() {
                return 1L;
            }

            public LocalDateTime getStart() {
                return now.plusDays(1);
            }

            public LocalDateTime getEnd() {
                return now.plusDays(2);
            }
        };

        Comment comment1 = Comment.builder()
                .id(1L)
                .text("Text")
                .author(owner)
                .item(item1)
                .created(now.minusHours(3))
                .build();

        Comment comment2 = Comment.builder()
                .id(2L)
                .text("Text2")
                .author(owner)
                .item(item2)
                .created(now.minusDays(1))
                .build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRepository.findByOwnerId(userId)).thenReturn(List.of(item1, item2));
        when(bookingRepository.findAllBookingsByOwnerId(userId))
                .thenReturn(List.of(pastBooking, futureBooking));
        when(commentRepository.findAll()).thenReturn(List.of(comment1, comment2));

        List<ItemWithBookingDto> result = itemService.getItems(userId);


        assertThat(result).hasSize(2);


        assertThat(result.get(0))
                .satisfies(item -> {
                    assertThat(item.getId()).isEqualTo(1L);
                    assertThat(item.getName()).isEqualTo("Name");
                    assertThat(item.getDescription()).isEqualTo("Description");
                    assertThat(item.getAvailable()).isTrue();
                    assertThat(item.getLastBooking()).isEqualTo(now.minusDays(1));
                    assertThat(item.getNextBooking()).isEqualTo(now.plusDays(1));
                    assertThat(item.getComments())
                            .hasSize(1)
                            .first()
                            .satisfies(comment -> {
                                assertThat(comment.getId()).isEqualTo(1L);
                                assertThat(comment.getText()).isEqualTo("Text");
                            });
                });

        // Проверяем второй предмет (только с комментарием)
        assertThat(result.get(1))
                .satisfies(item -> {
                    assertThat(item.getId()).isEqualTo(2L);
                    assertThat(item.getName()).isEqualTo("Name2");
                    assertThat(item.getDescription()).isEqualTo("Description2");
                    assertThat(item.getAvailable()).isFalse();
                    assertThat(item.getLastBooking()).isNull();
                    assertThat(item.getNextBooking()).isNull();
                    assertThat(item.getComments())
                            .hasSize(1)
                            .first()
                            .satisfies(comment -> {
                                assertThat(comment.getId()).isEqualTo(2L);
                                assertThat(comment.getText()).isEqualTo("Text2");
                            });
                });
    }

    @Test
    void searchItemsTest() {
        Long userId = 1L;
        String searchText = "search";

        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@ya.ru")
                .build();

        Item matchingItem1 = Item.builder()
                .id(1L)
                .name("Name")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        Item matchingItem2 = Item.builder()
                .id(2L)
                .name("Name2")
                .description("Description2")
                .available(true)
                .owner(owner)
                .build();

        Item nonMatchingItem = Item.builder()
                .id(3L)
                .name("Name3")
                .description("Description3")
                .available(true)
                .owner(owner)
                .build();

        when(itemRepository.search(searchText.toLowerCase())).thenReturn(List.of(matchingItem1, matchingItem2));

        List<ItemDto> result = itemService.findItems(userId, searchText);

        assertThat(result)
                .hasSize(2)
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder(
                        "Name",
                        "Name2"
                );

        verify(itemRepository).search(searchText.toLowerCase());
    }

    @Test
    void searchItemsWhenSearchTextIsNullTest() {
        Long userId = 1L;

        List<ItemDto> result = itemService.findItems(userId, null);

        assertThat(result).isEmpty();
        verify(itemRepository, never()).search(any());
    }

    @Test
    void searchItemsWhenSearchTextIsBlankTest() {
        Long userId = 1L;

        List<ItemDto> result1 = itemService.findItems(userId, "");
        List<ItemDto> result2 = itemService.findItems(userId, " ");

        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
        verify(itemRepository, never()).search(any());
    }

    @Test
    void addCommentTest() {
        Long userId = 1L;
        Long itemId = 10L;
        LocalDateTime now = LocalDateTime.now();

        User author = User.builder()
                .id(userId)
                .name("name")
                .email("mail@ya.ru")
                .build();

        Item item = Item.builder()
                .id(itemId)
                .name("item")
                .description("desc")
                .available(true)
                .build();

        CreateCommentDto createCommentDto = CreateCommentDto.builder()
                .text("text")
                .build();

        Comment savedComment = Comment.builder()
                .id(1L)
                .text("text")
                .author(author)
                .item(item)
                .created(now)
                .build();

        CommentDto expectedCommentDto = CommentDto.builder()
                .id(1L)
                .text("text")
                .authorName("name")
                .created(now)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(userId),
                eq(itemId),
                eq(BookingStatus.APPROVED),
                any(LocalDateTime.class))
        ).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentDto result = itemService.addComment(userId, itemId, createCommentDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("text");
        assertThat(result.getAuthorName()).isEqualTo("name");

        verify(commentRepository).save(argThat(comment ->
                comment.getText().equals("text") &&
                        comment.getAuthor().equals(author) &&
                        comment.getItem().equals(item)
        ));
    }

    @Test
    void addCommentUserNotFoundTest() {
        Long userId = 1L;
        Long itemId = 10L;
        CreateCommentDto commentDto = new CreateCommentDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(userId, itemId, commentDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Пользователь не найден");

        verify(itemRepository, never()).findById(any());
        verify(bookingRepository, never()).existsByBookerIdAndItemIdAndStatusAndEndBefore(any(), any(), any(), any());
    }

    @Test
    void addCommentItemNotFoundTest() {
        Long userId = 1L;
        Long itemId = 10L;
        CreateCommentDto commentDto = new CreateCommentDto();
        User author = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(userId, itemId, commentDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Предмет не найден");

        verify(bookingRepository, never()).existsByBookerIdAndItemIdAndStatusAndEndBefore(any(), any(), any(), any());
    }

    @Test
    void addCommentNoValidBookingsTest() {
        Long userId = 1L;
        Long itemId = 10L;
        CreateCommentDto commentDto = new CreateCommentDto();
        User author = User.builder().id(userId).build();
        Item item = Item.builder().id(itemId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                eq(userId),
                eq(itemId),
                eq(BookingStatus.APPROVED),
                any(LocalDateTime.class))
        ).thenReturn(false);

        assertThatThrownBy(() -> itemService.addComment(userId, itemId, commentDto))
                .isInstanceOf(ValidationException.class)
                .hasMessage("У пользователя нет подтвержденных букингов на данную вещь.");

        verify(commentRepository, never()).save(any());
    }
}