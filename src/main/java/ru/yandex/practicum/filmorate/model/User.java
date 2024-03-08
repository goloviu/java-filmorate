package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @EqualsAndHashCode.Exclude
    private Integer id;
    @NotBlank(message = "email пользователя не должен быть пустой, состоять из пробелов или неопределяться(null)")
    @Email(message = "Неверный формат записи почты пользователя")
    @NonNull
    private String email;
    @NotBlank(message = "Логин не может быть пустым и содержать пробелы")
    @NonNull
    private String login;
    @NonNull
    private String name;
    @NonNull
    private LocalDate birthday;
}
