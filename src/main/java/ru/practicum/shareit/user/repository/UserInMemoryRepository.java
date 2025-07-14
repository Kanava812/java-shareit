package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.user.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Repository
public class UserInMemoryRepository implements UserRepository {
    private final Map<Long, User> userStorage = new HashMap<>();
    private long generatedId = 0;

    @Override
    public User create(User user) {
        log.debug("Проверка уникальности почты.");
        emailValidation(user.getEmail());
        user.setId(++generatedId);
        userStorage.put(user.getId(), user);
        log.debug("Пользователь создан.");
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getEmail() == null) {
            log.debug("Нет новых данных для обновления почты.");
            userStorage.get(user.getId()).setName(user.getName());
            return userStorage.get(user.getId());
        }else{
            log.debug("Проверяем уникальность почты.");
            emailValidation(user.getEmail());
        }
        if (user.getName() == null) {
            log.debug("Нет новых данных для обновления имени.");
            userStorage.get(user.getId()).setEmail(user.getEmail());
            return userStorage.get(user.getId());
        }
        userStorage.put(user.getId(), user);
        log.debug("Пользователь обновлен.");
        return user;
    }

    @Override
    public User getUser(Long userId) {
        if (!userStorage.containsKey(userId)) {
            log.debug("Пользователь не найден.");
            throw new EntityNotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        log.debug("Пользователь с ID " + userId + " получен.");
        return userStorage.get(userId);
    }

    @Override
    public User delete(Long userId) {
        User userToDelete = userStorage.get(userId);
        userStorage.remove(userId);
        log.debug("Пользователь удален.");
        return userToDelete;
    }

    public void emailValidation(String email) {
        userStorage.values().stream()
                .filter(Objects::nonNull)
                .map(User::getEmail)
                .filter(e -> e.equals(email))
                .findFirst()
                .ifPresent(e -> {
                    throw new AlreadyExistsException("Пользователь с почтой: " + email +
                            " уже существует.");
                });
    }
}
