package ru.practicum.shareit.user.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUserTest() {
        CreateUserDto userToCreate = CreateUserDto.builder()
                .name("Name")
                .email("mail@ya.ru")
                .build();
        when(userService.create(userToCreate)).thenReturn(userToCreate);

        CreateUserDto actualUser = userController.create(userToCreate);

        assertEquals(userToCreate, actualUser);
        verify(userService).create(userToCreate);
    }

    @Test
    void updateUserTest() {
        UpdateUserDto userToUpdate = UpdateUserDto.builder()
                .name("Name")
                .email("mail@ya.ru")
                .build();
        UpdateUserDto updatedUser = UpdateUserDto.builder()
                .id(1L)
                .name("Name")
                .email("mail@ya.ru")
                .build();

        when(userService.update(updatedUser)).thenReturn(updatedUser);

        UpdateUserDto updatedUserFromService = userController.update(1L, userToUpdate);

        assertEquals(updatedUser, updatedUserFromService);
        verify(userService).update(userToUpdate);
    }

    @Test
    void getUserTest() {
        Long userId = 1L;
        UserDto expectedUser = UserDto.builder()
                .id(userId)
                .name("Name")
                .email("mail@ya.ru")
                .build();

        when(userService.getUser(userId)).thenReturn(expectedUser);

        UserDto actualUser = userController.getUser(userId);

        assertEquals(expectedUser, actualUser);
        verify(userService).getUser(userId);
    }

    @Test
    void deleteUserTest() {
        Long userId = 1L;
        UserDto expectedUser = UserDto.builder()
                .id(userId)
                .name("Name")
                .email("mail@ya.ru")
                .build();

        when(userService.delete(userId)).thenReturn(expectedUser);

        UserDto actualUser = userController.delete(userId);

        assertEquals(expectedUser, actualUser);
        verify(userService).delete(userId);
    }
}