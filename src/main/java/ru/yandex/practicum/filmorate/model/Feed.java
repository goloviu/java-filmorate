package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class Feed {
    @NotNull(message = "ID пользователя не задан (null)")
    private final Integer userId;
    @NotNull(message = "Тип события не задан (null)")
    private final EventType eventType;
    @NotNull(message = "Операция события не задана (null)")
    private final OperationType operation;
    @EqualsAndHashCode.Exclude
    private final Integer eventId;
    @NotNull(message = "ID сущности не задан (null)")
    private final Integer entityId;
    @NotNull(message = "Временная метка не задана (null)")
    private final Long timestamp;
}
