package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.BookingDates;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CreateCommentDto;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemDto addNewItem(Long userId, CreateItemDto item) {
        log.debug("Поиск пользователя с ID {}, создающего предмет.", userId);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден"));
        ItemRequest request = null;
        if (item.getRequestId() != null) {
            request = requestRepository.findById(item.getRequestId())
                    .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));
        }
        Item newItem = ItemMapper.toItemCreate(item);
        newItem.setOwner(user);
        newItem.setRequest(request);
        log.debug("Создание предмета {}.", item);
        return ItemMapper.toItemDto(itemRepository.save(newItem));
    }

    @Override
    public ItemDto updateItem(Long userId, UpdateItemDto item) {
        log.debug("Поиск пользователя с ID {}, обновляющего предмет.", userId);
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Пользователь не найден");
        }
        log.debug("Проверка прав пользователя с ID {} на обновление.", userId);
        Item updateItem = itemRepository.findById(item.getId())
                .orElseThrow(() -> new EntityNotFoundException("Вещь не найдена"));
        if (!userId.equals(updateItem.getOwner().getId())) {
            throw new EntityNotFoundException("Обновление отклонено. Пользователь с ID " + userId + " не является " +
                    "владельцем предмета: " + item);
        }
        if (item.getName() != null) {
            updateItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            updateItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            updateItem.setAvailable(item.getAvailable());
        }
        log.debug("Обновление предмета.");
        return ItemMapper.toItemDto(itemRepository.save(updateItem));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemWithBookingDto getItem(Long userId, Long itemId) {
        log.debug("Поиск пользователя с ID {}.", userId);
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Пользователь не найден");
        }
        log.debug("Получение предмета по ID {}.", itemId);
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException("Предмет не найден"));
        log.debug("Поиск дат букинга.");
        LocalDateTime lastBooking = null;
        LocalDateTime nextBooking = null;
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            List<BookingDates> allBookingsOfItem = bookingRepository.findAllBookingsByItemId(itemId);
            for (BookingDates booking : allBookingsOfItem) {
                if (booking.getEnd().isBefore(now)) {
                    lastBooking = booking.getEnd();
                } else if (booking.getStart().isAfter(now)) {
                    nextBooking = booking.getStart();
                    break;
                }
            }
        }
        ItemWithBookingDto itemDto = ItemMapper.toItemWithBookingDto(item, lastBooking, nextBooking);
        log.debug("Поиск всех комментариев к предмету.");
        List<CommentDto> commentsByItem = commentRepository.findAllByItemIdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        log.debug("Получение предмета.");
        itemDto.setComments(commentsByItem);
        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemWithBookingDto> getItems(Long userId) {
        log.debug("Поиск пользователя с ID {}.", userId);
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Пользователь не найден");
        }
        log.debug("Получение списка всех предметов пользователя с ID {}.", userId);
        List<Item> items = itemRepository.findByOwnerId(userId);
        List<BookingDates> bookingsDates = bookingRepository.findAllBookingsByOwnerId(userId);
        Map<Long, List<BookingDates>> bookingsByItem = bookingsDates.stream()
                .collect(Collectors.groupingBy(BookingDates::getItemId));
        List<Comment> comments = commentRepository.findAll();
        Map<Long, List<Comment>> commentsByItem = comments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        LocalDateTime now = LocalDateTime.now();
        return items.stream()
                .map(item -> {
                    List<BookingDates> itemBookings = bookingsByItem.getOrDefault(
                            item.getId(),
                            Collections.emptyList());
                    log.debug("Поиск дат букинга.");
                    LocalDateTime lastBooking = null;
                    LocalDateTime nextBooking = null;
                    for (BookingDates booking : itemBookings) {
                        if (booking.getEnd().isBefore(now)) {
                            lastBooking = booking.getEnd();
                        } else if (booking.getStart().isAfter(now)) {
                            nextBooking = booking.getStart();
                            break;
                        }
                    }
                    List<CommentDto> commentsDto = commentsByItem.getOrDefault(item.getId(),
                                    Collections.emptyList()).stream()
                            .map(CommentMapper::toCommentDto)
                            .toList();
                    ItemWithBookingDto itemDto = ItemMapper.toItemWithBookingDto(item, lastBooking, nextBooking);
                    itemDto.setComments(commentsDto);
                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> findItems(Long userId, String text) {
        if (text == null || text.isBlank()) {
            log.debug("Если текст для поиска не задан, результат- пустой список.");
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .filter(Objects::nonNull)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CreateCommentDto commentDto) {
        log.debug("Поиск пользователя с ID {}.", userId);
        User author = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Пользователь не найден")
        );
        log.debug("Поиск предмета с ID {}.", itemId);
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException("Предмет не найден"));
        log.debug("Поиск букингов предмета с ID {} пользователем с ID {}.", itemId, userId);

        boolean currentUserHasApprovedBookingForItem = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());
        if (!currentUserHasApprovedBookingForItem) {
            throw new ValidationException("У пользователя нет подтвержденных букингов на данную вещь.");
        }
        Comment comment = CommentMapper.toComment(commentDto, item, author);

        log.info("Создание комментария.");
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }
}
