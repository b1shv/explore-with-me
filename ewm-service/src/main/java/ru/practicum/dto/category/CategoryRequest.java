package ru.practicum.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Field: name. Error: must not be blank")
    @Size(min = 1, max = 50, message = "Field: name. Error: must be between 1 and 50 characters long")
    private String name;
}
