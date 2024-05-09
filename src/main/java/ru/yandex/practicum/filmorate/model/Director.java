package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Director {
    @EqualsAndHashCode.Exclude
    private Integer id;
    @NotBlank(message = "Имя режисера не может быть пустым или содержать только пробелы")
    private String name;

    public Map<String, Object> toMap() {
        Map<String, Object> dbMapping = new HashMap<>();

        dbMapping.put("name", this.getName());
        return dbMapping;
    }
}
