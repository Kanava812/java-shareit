package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {

    private Long id;

    @NotBlank(message = "Имя обязательно.")
    private String name;

    @NotBlank(message = "Адрес почты обязателен.")
    @Email(message = "Некорректный адрес почты.")
    private String email;
}
