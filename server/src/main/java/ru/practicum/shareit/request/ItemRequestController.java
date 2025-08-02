package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithAnswers;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService requestService;

    @GetMapping("/all")
    public List<ItemRequestDtoWithAnswers> getAllItemRequests() {
        return requestService.getAllItemRequests();
    }

    @GetMapping
    public List<ItemRequestDtoWithAnswers> getItemRequestsByRequestor(
            @RequestHeader("X-Sharer-User-Id") Long requestorId) {
        return requestService.getItemRequestsByRequestor(requestorId);
    }

    @GetMapping("/{id}")
    public ItemRequestDtoWithAnswers getItemRequestById(
            @PathVariable Long id) {
        return requestService.getItemRequestById(id);
    }

    @PostMapping
    public ItemRequestDto addNewItemRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody CreateItemRequestDto requestDto) {
        return requestService.addNewItemRequest(userId, requestDto);
    }
}
