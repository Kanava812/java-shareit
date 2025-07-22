package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public CreateUserDto create(CreateUserDto user) {
        log.debug("Создание пользователя.");
        return UserMapper.toUserDtoCreate(repository.save(UserMapper.toUserCreate(user)));
    }

    @Override
    public UpdateUserDto update(UpdateUserDto user) {
        if (user.getId() == null) {
            log.debug("Не указан ID.");
            throw new EntityNotFoundException("У пользователя не указан ID.");
        }
        if (user.getEmail() == null && user.getName() == null) {
            log.debug("Нет данных для обновления.");
            return UserMapper.toUserDtoUpdate(repository.findById(user.getId()).orElseThrow(
                    () -> new EntityNotFoundException("Пользователь не найден")
            ));
        }
        User existingUser = repository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        log.debug("Обновление пользователя.");
        return UserMapper.toUserDtoUpdate(repository.save(existingUser));
    }

    @Override
    public UserDto getUser(Long userId) {
        log.debug("Получение пользователя.");
        return UserMapper.toUserDto(repository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден")
        ));
    }

    @Override
    public UserDto delete(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        repository.delete(user);
        log.debug("Удаление пользователя.");
        return UserMapper.toUserDto(user);
    }
}
