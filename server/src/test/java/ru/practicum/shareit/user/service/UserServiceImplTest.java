package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserTest() {
        CreateUserDto userToCreate = CreateUserDto.builder()
                .id(1L)
                .name("Name")
                .email("mail@ya.ru")
                .build();
        User createdUser = User.builder()
                .id(1L)
                .name("Name")
                .email("mail@ya.ru")
                .build();
        when(repository.save(UserMapper.toUserCreate(userToCreate))).thenReturn(createdUser);

        CreateUserDto actualUser = userService.create(userToCreate);

        assertEquals(userToCreate, actualUser);
        assertThat(actualUser.getName()).isEqualTo(userToCreate.getName());
        assertThat(actualUser.getEmail()).isEqualTo(userToCreate.getEmail());
        verify(repository).save(UserMapper.toUserCreate(userToCreate));
    }

    @Test
    void updateUserTest() {
        Long userId = 1L;
        UpdateUserDto updateRequest = UpdateUserDto.builder()
                .id(userId)
                .name("Name")
                .email("mail@ya.ru")
                .build();

        User existingUser = User.builder()
                .id(userId)
                .name("Name2")
                .email("mail2@ya.ru")
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .name("Name2")
                .email("mail2@ya.ru")
                .build();

        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.save(any(User.class))).thenReturn(updatedUser);

        UpdateUserDto result = userService.update(updateRequest);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Name2");
        assertThat(result.getEmail()).isEqualTo("mail2@ya.ru");

        verify(repository).findById(userId);
        verify(repository).save(existingUser);
    }

    @Test
    void updateUserInvalidIdTest() {
        UpdateUserDto updateRequest = UpdateUserDto.builder()
                .id(null)
                .name("Name")
                .build();

        assertThatThrownBy(() -> userService.update(updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("У пользователя не указан ID.");

        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void updateUserNotFoundTest() {
        Long userId = 999L;
        UpdateUserDto updateRequest = UpdateUserDto.builder()
                .id(userId)
                .name("Name")
                .build();

        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(repository).findById(userId);
        verify(repository, never()).save(any());
    }

    @Test
    void updateUserNoChangesTest() {
        Long userId = 1L;
        UpdateUserDto updateRequest = UpdateUserDto.builder()
                .id(userId)
                .build();

        User existingUser = User.builder()
                .id(userId)
                .name("Name")
                .email("mail@ya.ru")
                .build();

        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));

        UpdateUserDto result = userService.update(updateRequest);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Name");
        assertThat(result.getEmail()).isEqualTo("mail@ya.ru");

        verify(repository).findById(userId);
        verify(repository, never()).save(any());
    }

    @Test
    void updateUserChangeNameTest() {
        Long userId = 1L;
        UpdateUserDto updateRequest = UpdateUserDto.builder()
                .id(userId)
                .name("Name")
                .build();

        User existingUser = User.builder()
                .id(userId)
                .name("Name2")
                .email("mail@ya.ru")
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .name("Name") // Новое имя
                .email("mail@ya.ru")
                .build();

        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(repository.save(any(User.class))).thenReturn(updatedUser);

        UpdateUserDto result = userService.update(updateRequest);

        assertThat(result.getName()).isEqualTo("Name");
        assertThat(result.getEmail()).isEqualTo("mail@ya.ru");

        verify(repository).save(argThat(user ->
                user.getName().equals("Name") && // Новое имя
                        user.getEmail().equals("mail@ya.ru")
        ));
    }

    @Test
    void getUserTest() {
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .name("Name")
                .email("mail@ya.ru")
                .build();

        UserDto expectedDto = UserMapper.toUserDto(existingUser);

        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));

        UserDto result = userService.getUser(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Name");
        assertThat(result.getEmail()).isEqualTo("mail@ya.ru");

        assertEquals(expectedDto, result);

        verify(repository).findById(userId);
    }

    @Test
    void getUserNotFoundTest() {
        Long nonExistentId = 100L;

        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(repository).findById(nonExistentId);
    }

    @Test
    void getUserInvalidIdTest() {
        when(repository.findById(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(repository).findById(null);
    }

    @Test
    void deleteUserTest() {
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .name("Name")
                .email("mail@ya.ru")
                .build();

        UserDto expectedDto = UserMapper.toUserDto(existingUser);

        when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(repository).delete(existingUser);

        UserDto result = userService.delete(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Name");
        assertThat(result.getEmail()).isEqualTo("mail@ya.ru");
        assertEquals(expectedDto, result);

        verify(repository).findById(userId);
        verify(repository).delete(existingUser);
    }

    @Test
    void deleteUserNotFoundTest() {
        Long nonExistentId = 100L;

        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(repository).findById(nonExistentId);
        verify(repository, never()).delete(any());
    }

    @Test
    void deleteUserInvalidIdTest() {
        when(repository.findById(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(repository).findById(null);
        verify(repository, never()).delete(any());
    }
}