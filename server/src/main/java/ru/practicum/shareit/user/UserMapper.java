package ru.practicum.shareit.user;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

@UtilityClass
public class UserMapper {
    public CreateUserDto toUserDtoCreate(User user) {
        return CreateUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public User toUserCreate(CreateUserDto createUserDto) {
        return User.builder()
                .name(createUserDto.getName())
                .email(createUserDto.getEmail())
                .build();
    }

    public UpdateUserDto toUserDtoUpdate(User user) {
        return UpdateUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
