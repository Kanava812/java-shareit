package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDto {

    @NotBlank(message = "Комментарий не может быть пустым.")
    @Size(min = 1, max = 500, message = "Длина комментария должна быть от 1 до 500 символов.")
    private String text;
}