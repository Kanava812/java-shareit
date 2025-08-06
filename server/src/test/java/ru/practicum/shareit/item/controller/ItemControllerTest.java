package ru.practicum.shareit.item.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    @Test
    void createItemTest() {
        Long userId = 1L;
        CreateItemDto itemToCreate = CreateItemDto.builder()
                .name("Name")
                .description("Description")
                .available(true)
                .requestId(1L)
                .build();

        ItemDto createdItem = ItemDto.builder()
                .id(1L)
                .name("Name")
                .description("Description")
                .available(true)
                .build();

        when(itemService.addNewItem(userId, itemToCreate)).thenReturn(createdItem);

        ItemDto actualItem = itemController.addNewItem(userId, itemToCreate);

        assertEquals(createdItem, actualItem);
        verify(itemService).addNewItem(anyLong(), any(CreateItemDto.class));
    }

    @Test
    void updateItemTest() {
        Long itemId = 1L;
        Long userId = 1L;
        UpdateItemDto itemToUpdate = UpdateItemDto.builder()
                .name("Name")
                .description("Description")
                .available(true)
                .build();

        ItemDto updatedItem = ItemDto.builder()
                .id(1L)
                .name("Name")
                .description("Description")
                .available(true)
                .build();

        when(itemService.updateItem(userId, itemToUpdate)).thenReturn(updatedItem);

        ItemDto updatedItemFromService = itemController.updateItem(userId, itemId, itemToUpdate);

        assertEquals(updatedItem, updatedItemFromService);
        verify(itemService).updateItem(anyLong(), any(UpdateItemDto.class));
    }

    @Test
    void getItemTest() {
        Long itemId = 1L;
        Long userId = 1L;
        List<CommentDto> comments = new ArrayList<>(Collections.emptyList());

        ItemWithBookingDto item = ItemWithBookingDto.builder()
                .id(itemId)
                .name("Name")
                .description("Description")
                .available(true)
                .lastBooking(LocalDateTime.now().minusSeconds(6400))
                .nextBooking(LocalDateTime.now().plusSeconds(6400))
                .comments(comments)
                .build();

        when(itemService.getItem(userId, itemId)).thenReturn(item);

        ItemWithBookingDto returnedItem = itemController.getItem(userId, itemId);

        assertEquals(returnedItem, item);
        verify(itemService).getItem(userId, itemId);
    }

    @Test
    void listItemsTest() {
        Long userId = 1L;
        List<ItemWithBookingDto> expectedItems = List.of(
                ItemWithBookingDto.builder()
                        .id(1L)
                        .name("Name 1")
                        .description("Description 1")
                        .available(true)
                        .build(),
                ItemWithBookingDto.builder()
                        .id(2L)
                        .name("Name 2")
                        .description("Description 2")
                        .available(false)
                        .build()
        );

        when(itemService.getItems(userId)).thenReturn(expectedItems);

        List<ItemWithBookingDto> actualItems = itemController.getItems(userId);

        assertThat(actualItems).hasSize(2);
        assertThat(actualItems).isEqualTo(expectedItems);
        verify(itemService).getItems(userId);
    }

    @Test
    void searchItemsTest() {
        Long userId = 1L;
        String searchText = "test";
        List<ItemDto> expectedItems = List.of(
                ItemDto.builder()
                        .id(1L)
                        .name("TestName")
                        .description("TestDescription")
                        .available(true)
                        .build()
        );

        when(itemService.findItems(userId, searchText)).thenReturn(expectedItems);

        List<ItemDto> actualItems = itemController.findItems(userId, searchText);

        assertThat(actualItems).hasSize(1);
        assertThat(actualItems.getFirst().getName().toLowerCase()).contains(searchText);
        assertThat(actualItems.getFirst().getDescription().toLowerCase()).contains(searchText);
        verify(itemService).findItems(userId, searchText);
    }

    @Test
    void postCommentTest() {
        Long userId = 1L;
        Long itemId = 1L;
        CreateCommentDto newComment = CreateCommentDto.builder()
                .text("text")
                .build();

        CommentDto expectedComment = CommentDto.builder()
                .id(1L)
                .text("text")
                .authorName("Name")
                .created(LocalDateTime.now())
                .build();

        when(itemService.addComment(userId, itemId, newComment)).thenReturn(expectedComment);

        CommentDto actualComment = itemController.addComment(userId, itemId, newComment);

        assertThat(actualComment.getText()).isEqualTo(newComment.getText());
        assertThat(actualComment.getId()).isNotNull();
        verify(itemService).addComment(userId, itemId, newComment);
    }
}