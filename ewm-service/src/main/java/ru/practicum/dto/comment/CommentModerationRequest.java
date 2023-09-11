package ru.practicum.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.model.state.CommentUpdateState;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentModerationRequest {
    @NotEmpty(message = "Field: commentIds. Error: must not be empty")
    private List<Long> commentIds;

    @NotNull(message = "Field: status. Error: must not be null")
    private CommentUpdateState status;
}
