package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithAnswers;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addNewItemRequest(Long userId, CreateItemRequestDto requestDto);

    List<ItemRequestDtoWithAnswers> getItemRequestsByRequestor(Long requestorId);

    List<ItemRequestDtoWithAnswers> getAllItemRequests(Long userId);

    ItemRequestDtoWithAnswers getItemRequestById(Long userId, Long id);
}
