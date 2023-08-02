package ru.practicum.controller.params;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Pageable;
import ru.practicum.model.state.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SearchParamsAdmin {
    private List<Long> users;
    private List<EventState> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Pageable pageable;
}
