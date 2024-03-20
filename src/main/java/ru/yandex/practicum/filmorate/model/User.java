package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @EqualsAndHashCode.Exclude
    private Integer id;
    @NotBlank(message = "email пользователя не должен быть пустой, состоять из пробелов или не определяться(null)")
    @Email(message = "Неверный формат записи почты пользователя")
    private String email;
    @NotBlank(message = "Логин не может быть пустым и содержать пробелы")
    private String login;
    private String name;
    @NonNull
    private LocalDate birthday;
    private Set<Integer> friends = new HashSet<>();
}
