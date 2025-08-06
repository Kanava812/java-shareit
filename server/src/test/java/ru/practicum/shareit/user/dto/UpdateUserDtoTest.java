package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UpdateUserDtoTest {
    @Autowired
    private JacksonTester<UpdateUserDto> json;

    @Test
    void serializeUpdateUserDtoTest() throws Exception {
        UpdateUserDto updateDto = UpdateUserDto.builder()
                .id(1L)
                .name("Name")
                .email("mail@ya.ru")
                .build();

        JsonContent<UpdateUserDto> result = json.write(updateDto);

        assertThat(result).hasJsonPathNumberValue("@.id");
        assertThat(result).hasJsonPathStringValue("@.name");
        assertThat(result).hasJsonPathStringValue("@.email");

        assertThat(result).extractingJsonPathNumberValue("@.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("@.name").isEqualTo("Name");
        assertThat(result).extractingJsonPathStringValue("@.email").isEqualTo("mail@ya.ru");

        assertThat(result).isEqualToJson(
                "{\n" +
                        "    \"id\": 1,\n" +
                        "    \"name\": \"Name\",\n" +
                        "    \"email\": \"mail@ya.ru\"\n" +
                        "}"
        );
    }

    @Test
    void deserializeUpdateUserDtoTest() throws Exception {
        String jsonContent = "{\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"Name\",\n" +
                "    \"email\": \"mail@ya.ru\"\n" +
                "}";

        UpdateUserDto parsedDto = json.parse(jsonContent).getObject();

        assertThat(parsedDto.getId()).isEqualTo(null);
        assertThat(parsedDto.getName()).isEqualTo("Name");
        assertThat(parsedDto.getEmail()).isEqualTo("mail@ya.ru");
    }
}