package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

@Slf4j
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addNewItem(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody CreateItemDto item) {
        log.debug("Создание предмета {}.", item);
        return itemClient.addNewItem(userId, item);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable @Positive Long itemId,
                                             @Valid @RequestBody UpdateItemDto item) {
        log.debug("Обновление предмета c ID {} ({}), принадлежащего пользователю с ID {}.", itemId, item, userId);
        item.setId(itemId);
        return itemClient.updateItem(userId, itemId, item);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable @Positive Long itemId) {
        log.debug("Получение предмета с ID {} пользователем с ID {}.", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Получение списка всех предметов пользователя ID {}.", userId);
        return itemClient.getItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findItems(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam String text) {
        log.debug("Получение пользователем с ID {} списка предметов, содержащих текст: {}.", userId, text);
        return itemClient.findItems(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @PathVariable @Positive Long itemId,
                                             @Valid @RequestBody CreateCommentDto comment) {
        log.debug("Комментарий пользователя с ID {} к предмету c ID {}: {}", userId, itemId, comment);
        return itemClient.addComment(userId, itemId, comment);
    }
}

