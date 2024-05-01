package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class Review {
    @EqualsAndHashCode.Exclude
    private Integer reviewId;
    @NotNull(message = "Контент отзыва не задан (null)")
    private String content;
    @NotNull(message = "Оценка отзыва не задана (null)")
    private Boolean isPositive;
    @NotNull(message = "Автор отзыва не задан (null)")
    private Integer userId;
    @NotNull(message = "Фильм отзыва не задан (null)")
    private Integer filmId;
    private Integer useful;

    public Map<String, Object> toMap() {
        Map<String, Object> dbMapping = new HashMap<>();

        dbMapping.put("content", this.getContent());
        dbMapping.put("is_positive", this.getIsPositive());
        dbMapping.put("user_Id", this.getUserId());
        dbMapping.put("movie_id", this.getFilmId());
        dbMapping.put("useful", this.getUseful());
        return dbMapping;
    }
}
