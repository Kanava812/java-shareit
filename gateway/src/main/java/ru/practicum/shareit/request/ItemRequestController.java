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
    public ResponseEntity<Object> getAllItemRequests() {
        return requestClient.getAllItemRequests();
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestsByRequestor(
            @RequestHeader("X-Sharer-User-Id") Long requestorId) {
        return requestClient.getItemRequestsByRequestor(requestorId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemRequestById(
            @PathVariable @Positive Long id) {
        return requestClient.getItemRequestById(id);
    }

    @PostMapping
    public ResponseEntity<Object> addNewItemRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody CreateItemRequestDto requestDto) {
        return requestClient.addNewItemRequest(userId, requestDto);
    }
}
