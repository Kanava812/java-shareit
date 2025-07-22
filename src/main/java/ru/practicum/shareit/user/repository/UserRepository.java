package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.User;

public interface UserRepository {
    User create(User user);

    User update(User user);

    User getUser(Long userId);

    User delete(Long userId);

    void emailValidation(String email);
}
