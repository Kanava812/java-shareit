package ru.practicum.shareit.request.dto;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestDtoWithAnswersTest {
    private final JacksonTester<ItemRequestDtoWithAnswers> json;

    @Test
    void serializedItemRequestWithResponseDtoTest() throws Exception {
        ItemForRequestDto item = ItemForRequestDto.builder()
                .id(1L)
                .name("Name")
                .ownerId(2L)
                .build();

        ItemRequestDtoWithAnswers request = ItemRequestDtoWithAnswers.builder()
                .id(1L)
                .description("Text")
                .created(LocalDateTime.of(2025, 5, 5, 5, 5))
                .items(List.of(item))
                .build();

        JsonContent<ItemRequestDtoWithAnswers> result = json.write(request);

        assertThat(result).extractingJsonPathNumberValue("@.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("@.description").isEqualTo("Text");
        assertThat(result).extractingJsonPathStringValue("@.created").isEqualTo("2025-05-05T05:05:00");

        assertThat(result).extractingJsonPathArrayValue("@.items").hasSize(1);
        assertThat(result).extractingJsonPathNumberValue("@.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("@.items[0].name").isEqualTo("Name");
        assertThat(result).extractingJsonPathNumberValue("@.items[0].ownerId").isEqualTo(2);
    }

    @Test
    void deserializedItemRequestWithResponseDtoTest() throws Exception {
        String jsonContent = "{\n" +
                "    \"id\": 1,\n" +
                "    \"description\": \"Text\",\n" +
                "    \"created\": \"2025-01-01T01:00:00\",\n" +
                "    \"items\": [\n" +
                "        {\n" +
                "            \"id\": 1,\n" +
                "            \"name\": \"Name\",\n" +
                "            \"ownerId\": 2\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        ItemRequestDtoWithAnswers parsedDto = json.parse(jsonContent).getObject();

        assertThat(parsedDto.getId()).isEqualTo(1L);
        assertThat(parsedDto.getDescription()).isEqualTo("Text");
        assertThat(parsedDto.getCreated()).isEqualTo(LocalDateTime.of(2025, 1, 1, 1, 0));

        assertThat(parsedDto.getItems())
                .hasSize(1)
                .first()
                .extracting(
                        ItemForRequestDto::getId,
                        ItemForRequestDto::getName,
                        ItemForRequestDto::getOwnerId
                        )
                .containsExactly(1L, "Name", 2L);
    }

}