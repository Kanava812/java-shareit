package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Repository
public class ItemInMemoryRepository implements ItemRepository {
    private final Map<Long, Item> itemStorage = new HashMap<>();
    private long generatedId = 0;

    @Override
    public Item addNewItem(User user, Item item) {
        item.setId(++generatedId);
        item.setOwner(user);
        itemStorage.put(item.getId(), item);
        log.debug("Предмет создан.");
        return item;
    }

    @Override
    public Item updateItem(Long userId, Item item) {
        log.debug("Обновление предмета.");
        if (item.getName() != null) {
            itemStorage.get(item.getId()).setName(item.getName());
        }
        if (item.getDescription() != null) {
            itemStorage.get(item.getId()).setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemStorage.get(item.getId()).setAvailable(item.getAvailable());
        }
        log.debug("Предмет обновлен.");
        return itemStorage.get(item.getId());
    }

    @Override
    public Item getItem(Long itemId) {
        if (itemId == null || !itemStorage.containsKey(itemId)) {
            log.debug("Предмет не найден.");
            throw new EntityNotFoundException("Предмет c ID: " + itemId + " не найден");
        }
        log.debug("Получение предмета по ID {}.", itemId);
        return itemStorage.get(itemId);
    }

    @Override
    public List<Item> getItems(Long userId) {
        log.debug("Получение списка всех предметов пользователя с ID {}.", userId);
        return itemStorage.values().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getOwner().getId().equals(userId))
                .toList();
    }

    @Override
    public List<Item> findItems(Long userId, String text) {
        String findText = text.toLowerCase();
        log.debug("Поиск предмета пользователем с ID {} по тексту: {} .", userId, text);
        return itemStorage.values().stream()
                .filter(Objects::nonNull)
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(findText) ||
                        item.getDescription().toLowerCase().contains(findText))
                .toList();
    }
}
