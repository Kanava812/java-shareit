package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserDto {

    private Long id;

    @NotBlank(message = "Имя обязательно.")
    private String name;

    @NotBlank(message = "Адрес почты обязателен.")
    @Email(message = "Некорректный адрес почты.")
    private String email;
}
