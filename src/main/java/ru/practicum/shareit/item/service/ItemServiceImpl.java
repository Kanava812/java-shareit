package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;


    @Override
    public ItemDto addNewItem(Long userId, CreateItemDto item) {
        log.debug("Поиск пользователя с ID {}, создающего предмет.", userId);
        User user = userRepository.getUser(userId);
        log.debug("Создание предмета {}.", item);
        return ItemMapper.toItemDto(itemRepository.addNewItem(user, ItemMapper.toItemCreate(item)));
    }

    @Override
    public ItemDto updateItem(Long userId, UpdateItemDto item) {
        log.debug("Поиск пользователя с ID {}, обновляющего предмет.", userId);
        User user = userRepository.getUser(userId);
        log.debug("Проверка прав пользователя с ID {} на обновление.", userId);
        if (!userId.equals(itemRepository.getItem(item.getId()).getOwner().getId())) {
            throw new EntityNotFoundException("Обновление отклонено. Пользователь с ID " + userId + " не является " +
                    "владельцем предмета: " + item);
        }
        log.debug("Обновление предмета.");
        return ItemMapper.toItemDto(itemRepository.updateItem(userId, ItemMapper.toItemUpdate(item)));
    }

    @Override
    public ItemDto getItem(Long userId, Long itemId) {
        log.debug("Поиск пользователя с ID {}.", userId);
        User user = userRepository.getUser(userId);
        log.debug("Получение предмета по ID {}.", itemId);
        return ItemMapper.toItemDto(itemRepository.getItem(itemId));
    }

    @Override
    public List<ItemDto> getItems(Long userId) {
        log.debug("Поиск пользователя с ID {}.", userId);
        User user = userRepository.getUser(userId);
        log.debug("Получение списка всех предметов пользователя с ID {}.", userId);
        return itemRepository.getItems(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> findItems(Long userId, String text) {
        if (text == null || text.isBlank()) {
            log.debug("Если текст для поиска не задан, результат- пустой список.");
            return Collections.emptyList();
        }
        return itemRepository.findItems(userId, text).stream()
                .filter(Objects::nonNull)
                .map(ItemMapper::toItemDto)
                .toList();
    }
}
