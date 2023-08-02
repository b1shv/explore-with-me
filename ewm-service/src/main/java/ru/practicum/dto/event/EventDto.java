package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.validation.Create;
import ru.practicum.validation.Update;
import ru.practicum.validation.ValidEventDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ValidEventDate(groups = {Create.class, Update.class})
public class EventDto {
    @NotBlank(groups = {Create.class}, message = "Field: title. Error: must not be blank")
    @Size(groups = {Create.class, Update.class}, min = 3, max = 120, message = "Field: title. Error: must be between 3 and 120 characters long")
    private String title;

    @NotBlank(groups = {Create.class}, message = "Field: annotation. Error: must not be blank")
    @Size(groups = {Create.class, Update.class}, min = 20, max = 2000, message = "Field: annotation. Error: must be between 20 and 2000 characters long")
    private String annotation;

    @NotNull(groups = {Create.class}, message = "Field: category. Error: must not be null")
    private long category;

    @NotBlank(groups = {Create.class}, message = "Field: description. Error: must not be blank")
    @Size(groups = {Create.class, Update.class}, min = 20, max = 7000, message = "Field: description. Error: must be between 20 and 7000 characters long")
    private String description;

    @NotNull(groups = {Create.class}, message = "Field: location. Error: must not be null")
    private Location location;

    @NotNull(groups = {Create.class}, message = "Field: eventDate. Error: must not be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
}
