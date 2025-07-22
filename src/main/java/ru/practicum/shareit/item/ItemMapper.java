package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.time.LocalDateTime;

@UtilityClass
public class ItemMapper {
    public Item toItemCreate(CreateItemDto createItemDto) {
        return Item.builder()
                .name(createItemDto.getName())
                .description(createItemDto.getDescription())
                .available(createItemDto.getAvailable())
                .build();
    }

    public Item toItemUpdate(UpdateItemDto updateItemDto) {
        return Item.builder()
                .id(updateItemDto.getId())
                .name(updateItemDto.getName())
                .description(updateItemDto.getDescription())
                .available(updateItemDto.getAvailable())
                .build();
    }

    public ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public ItemWithBookingDto toItemWithBookingDto(Item item, LocalDateTime lastBooking, LocalDateTime nextBooking) {
        return ItemWithBookingDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .build();
    }
}
