package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping(path = "/users")
@Validated
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody CreateUserDto user) {
        log.debug("Cоздание пользователя: {}.", user);
        return userClient.create(user);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@Min(1L) @PathVariable @Positive Long userId,
                                         @Valid @RequestBody UpdateUserDto user) {
        log.debug("Обновление пользователя c ID {}. Новые данные: {}.", userId, user);
        user.setId(userId);
        return userClient.update(userId, user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@Min(1L) @PathVariable @Positive Long userId) {
        log.debug("Получение пользователя с ID {}.", userId);
        return userClient.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@Min(1L) @PathVariable @Positive Long userId) {
        log.debug("Удаление пользователя с ID {}.", userId);
        return userClient.delete(userId);
    }
}
