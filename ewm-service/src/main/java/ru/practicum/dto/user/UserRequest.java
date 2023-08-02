package ru.practicum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @NotBlank(message = "Field: name. Error: must not be blank")
    @Size(min = 2, max = 250, message = "Field: name. Error: must be between 2 and 250 characters long")
    private String name;

    @NotBlank(message = "Field: email. Error: must not be blank")
    @Email(message = "Field: email. Error: must be email")
    @Size(min = 6, max = 254, message = "Field: email. Error: must be between 6 and 254 characters long")
    private String email;
}
