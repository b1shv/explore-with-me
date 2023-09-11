package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.state.event.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private String description;
    private Location location;
    private boolean paid;
    private int participantLimit;
    private boolean requestModeration;
    private boolean commentModeration;
    private int confirmedRequests;
    private UserShortDto initiator;
    private EventState state;
    private long views;
    private List<CommentDto> comments;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    private LocalDateTime eventDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    private LocalDateTime createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    private LocalDateTime publishedOn;
}
