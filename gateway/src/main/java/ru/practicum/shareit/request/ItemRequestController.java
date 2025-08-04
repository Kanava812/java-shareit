package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

@Slf4j
@RequiredArgsConstructor
@Validated
@Controller
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return requestClient.getAllItemRequests(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestsByRequestor(
            @RequestHeader("X-Sharer-User-Id") @Positive Long requestorId) {
        return requestClient.getItemRequestsByRequestor(requestorId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemRequestById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long id) {
        return requestClient.getItemRequestById(userId, id);
    }

    @PostMapping
    public ResponseEntity<Object> addNewItemRequest(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @Valid @RequestBody CreateItemRequestDto requestDto) {
        return requestClient.addNewItemRequest(userId, requestDto);
    }
}
