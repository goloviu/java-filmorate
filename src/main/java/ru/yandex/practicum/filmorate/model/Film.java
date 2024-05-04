package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;

@Data
@NoArgsConstructor // для сериализации Json в объект
@AllArgsConstructor
@Builder
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
    @Valid
    private FilmRating mpa;
    @NonNull
    @Valid
    private Set<FilmGenre> genres = new HashSet<>();
    private Set<Integer> usersLikes = new HashSet<>();
    @NonNull
    private List<Director> directors = new ArrayList<>();

    public Map<String, Object> toMap() {
        Map<String, Object> dbMapping = new HashMap<>();

        dbMapping.put("title", this.getName());
        dbMapping.put("description", this.getDescription());
        dbMapping.put("duration", this.getDuration());
        dbMapping.put("release_date", this.getReleaseDate());
        dbMapping.put("rating_id", this.getMpa().getId());
        return dbMapping;
    }
}
