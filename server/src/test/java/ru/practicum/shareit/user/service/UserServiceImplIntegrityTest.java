package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(UserServiceImpl.class)
class UserServiceImplIntegrityTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .name("Name")
                .email("mail@ya.ru")
                .build());
    }

    @Test
    @Transactional
    void createShouldSaveUserTest() {
        CreateUserDto dto = CreateUserDto.builder()
                .name("Name2")
                .email("mail2@ya.ru")
                .build();

        CreateUserDto result = userService.create(dto);

        assertThat(result.getId()).isNotNull();
        assertThat(userRepository.findAll()).hasSize(2);
        assertThat(result.getEmail()).isEqualTo("mail2@ya.ru");
        assertThat(result.getName()).isEqualTo("Name2");
    }

    @Test
    @Transactional
    void updateShouldModifyExistingUserTest() {
        UpdateUserDto updateDto = UpdateUserDto.builder()
                .id(testUser.getId())
                .name("Updated Name")
                .build();

        UpdateUserDto result = userService.update(updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(userRepository.findById(testUser.getId()).get().getName())
                .isEqualTo("Updated Name");
    }

    @Test
    void getUserShouldReturnUserFromDbTest() {
        UserDto user = userService.getUser(testUser.getId());

        assertThat(user.getId()).isEqualTo(testUser.getId());
        assertThat(user.getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void deleteShouldRemoveUserFromDbTest() {
        userService.delete(testUser.getId());

        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }
}