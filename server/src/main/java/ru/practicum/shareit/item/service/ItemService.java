package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import java.util.List;

public interface ItemService {
    ItemDto addNewItem(Long userId, CreateItemDto item);

    ItemDto updateItem(Long userId, UpdateItemDto item);

    ItemWithBookingDto getItem(Long userId, Long itemId);

    List<ItemWithBookingDto> getItems(Long userId);

    List<ItemDto> findItems(Long userId, String text);

    CommentDto addComment(Long userId, Long itemId, CreateCommentDto comment);
}
