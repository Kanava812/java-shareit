package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemDto {
    @NotBlank(message = "Название обязательно.")
    private String name;

    @NotBlank(message = "Описание обязательно.")
    private String description;

    @NotNull(message = "Значение доступности предмета обязательно.")
    private Boolean available;

    private Long requestId;
}
