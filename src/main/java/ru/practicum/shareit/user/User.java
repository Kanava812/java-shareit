package ru.practicum.shareit.user;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id", "email"})
public class User {
    private Long id;
    private String name;
    private String email;
}