package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.service.ItemService;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto addNewItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @Valid @RequestBody CreateItemDto item) {
        log.debug("Создание предмета {}.", item);
        return itemService.addNewItem(userId, item);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable @Positive Long itemId,
                              @Valid @RequestBody UpdateItemDto item) {
        log.debug("Обновление предмета c ID {} ({}), принадлежащего пользователю" +
                "с ID {}.", itemId, item, userId);
        item.setId(itemId);
        return itemService.updateItem(userId, item);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable @Positive Long itemId) {
        log.debug("Получение предмета с ID {} пользователем с ID {}.", itemId, userId);
        return itemService.getItem(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Получение списка всех предметов пользователя ID {}.", userId);
        return itemService.getItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @RequestParam String text) {
        log.debug("Получение пользователем с ID {} списка предметов, содержащих текст: {}.", userId, text);
        return itemService.findItems(userId, text);
    }
}

