package ru.practicum.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.validation.Create;
import ru.practicum.validation.Update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompilationRequest {
    @NotBlank(groups = {Create.class}, message = "Field: title. Error: must not be blank")
    @Size(groups = {Create.class, Update.class}, min = 1, max = 50,
            message = "Field: name. Must be between 1 and 50 characters long")
    private String title;

    Boolean pinned;
    List<Long> events;
}
