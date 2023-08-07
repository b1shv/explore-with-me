package ru.practicum.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.model.state.RequestUpdateState;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestModerationRequest {
    @NotEmpty(message = "Field: requestIds. Error: must not be empty")
    private List<Long> requestIds;

    @NotNull(message = "Field: status. Error: must not be null")
    private RequestUpdateState status;
}
