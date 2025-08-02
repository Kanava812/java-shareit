package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.RequestMapper;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithAnswers;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements  ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;

    public ItemRequestDto addNewItemRequest(Long userId, CreateItemRequestDto requestDto) {
        log.info("Создание нового запроса: '{}' пользователем с ID {}.", requestDto.getDescription(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        ItemRequest request = new ItemRequest();
        request.setRequestor(user);
        request.setDescription(requestDto.getDescription());
        request.setCreated(LocalDateTime.now());

        ItemRequest saved = requestRepository.save(request);
        log.info("Запрос создан: {}", saved);
        return RequestMapper.toItemRequestDto(saved);
    }

    public List<ItemRequestDtoWithAnswers> getItemRequestsByRequestor(Long requestorId) {
        log.info("Получение списка своих запросов вместе с данными об ответах на них пользователем с ID {}.",
                requestorId);
        User user = userRepository.findById(requestorId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        List<ItemRequest> list = requestRepository.findAllByRequestorIdOrderByCreatedDesc(requestorId);
        return list.stream().map(RequestMapper::toItemRequestWithAnswersDto).toList();
    }

    public List<ItemRequestDtoWithAnswers> getAllItemRequests() {
        log.info("Получение списка запросов, созданных другими пользователями.");
        return requestRepository.findAll(Sort.by(Sort.Direction.DESC, "created"))
                .stream().map(RequestMapper::toItemRequestWithAnswersDto)
                .toList();
    }

    public ItemRequestDtoWithAnswers getItemRequestById(Long id) {
        log.info("Получение данных о запросе c ID {} вместе с данными об ответах на него", id);
        ItemRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Запрос не найден."));
        List<Item> items = itemRepository.findByRequestId(id, Sort.by(Item.Fields.id));
        request.setItems(items);
        return RequestMapper.toItemRequestWithAnswersDto(request);
    }
}
