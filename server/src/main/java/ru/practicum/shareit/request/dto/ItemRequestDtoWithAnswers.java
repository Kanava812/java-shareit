package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.practicum.shareit.item.dto.ItemForRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestDtoWithAnswers {
    private Long id;

    private String description;

    private Long userId;

    @JsonProperty(value = "created")
    private LocalDateTime created;

    List<ItemForRequestDto> items = new ArrayList<>();
}