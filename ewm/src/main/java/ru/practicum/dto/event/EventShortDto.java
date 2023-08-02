package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
public class EventShortDto {
    private long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private String description;
    private boolean paid;
    private int confirmedRequests;
    private UserShortDto initiator;
    private long views;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
}

