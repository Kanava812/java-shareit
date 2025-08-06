package ru.practicum.shareit.user.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CreateUserDtoTest {
    private final JacksonTester<CreateUserDto> json;

    @Test
    void serializeCreateUserDtoTest() throws Exception {
        CreateUserDto userToCreate = CreateUserDto.builder()
                .id(1L)
                .name("Name")
                .email("mail@ya.ru")
                .build();

        JsonContent<CreateUserDto> result = json.write(userToCreate);

        assertThat(result).hasJsonPathNumberValue("@.id");
        assertThat(result).hasJsonPathStringValue("@.name");
        assertThat(result).hasJsonPathStringValue("@.email");

        assertThat(result).extractingJsonPathNumberValue("@.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("@.name").isEqualTo("Name");
        assertThat(result).extractingJsonPathStringValue("@.email").isEqualTo("mail@ya.ru");

    }

    @Test
    void deserializeCreateUserDtoTest() throws Exception {
        String jsonContent = "{\n" +
                "\"id\": 1,\n" +
                "\"name\": \"Name\",\n" +
                "\"email\": \"mail@ya.ru\"\n" +
                "}";

        CreateUserDto parsedDto = json.parse(jsonContent).getObject();

        assertThat(parsedDto.getId()).isEqualTo(1L);
        assertThat(parsedDto.getName()).isEqualTo("Name");
        assertThat(parsedDto.getEmail()).isEqualTo("mail@ya.ru");
    }
}