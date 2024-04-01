package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor // для сериализации Json в объект
@AllArgsConstructor
public class Film {

    @EqualsAndHashCode.Exclude
    private Integer id;
    @NotBlank(message = "Название фильма не может быть пустым или содержать только пробелы")
    private String name;
    @NotNull(message = "Описание фильма не задано(null)")
    @Size(max = 200, message = "Размер описания фильма не может превышать 200 символов")
    private String description;
    @NonNull
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма не может быть отрицательной")
    @NonNull
    private Integer duration;
    @NonNull
    private FilmGenre genre;
    @NonNull
    private FilmRating rating;
    private Set<Integer> usersLikes = new HashSet<>();
}
