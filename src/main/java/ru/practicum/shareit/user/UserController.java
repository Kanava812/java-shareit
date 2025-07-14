package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public CreateUserDto create(@Valid @RequestBody CreateUserDto user) {
        log.debug("Cоздание пользователя: {}.", user);
        return userService.create(user);
    }

    @PatchMapping("/{userId}")
    public UpdateUserDto update(@PathVariable @Positive Long userId,
                                @Valid @RequestBody UpdateUserDto user) {
        log.debug("Обновление пользователя c ID {}. Новые данные: {}.", userId, user);
        user.setId(userId);
        return userService.update(user);
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable @Positive Long userId) {
        log.debug("Получение пользователя с ID {}.", userId);
        return userService.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    public UserDto delete(@PathVariable @Positive Long userId) {
        log.debug("Удаление пользователя с ID {}.", userId);
        return userService.delete(userId);
    }
}
