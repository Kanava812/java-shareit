package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithAnswers;

@UtilityClass
public class RequestMapper {
    public ItemRequestDto toItemRequestDto(ItemRequest request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .userId(request.getRequestor().getId())
                .created(request.getCreated())
                .description(request.getDescription())
                .build();
    }

    public ItemRequestDtoWithAnswers toItemRequestWithAnswersDto(ItemRequest request) {
        return ItemRequestDtoWithAnswers.builder()
                .id(request.getId())
                .userId(request.getRequestor().getId())
                .created(request.getCreated())
                .description(request.getDescription())
                .build();
    }
}
