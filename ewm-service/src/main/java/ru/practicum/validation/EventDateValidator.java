package ru.practicum.validation;

import ru.practicum.dto.event.EventDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class EventDateValidator implements ConstraintValidator<ValidEventDate, EventDto> {

    @Override
    public void initialize(ValidEventDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(EventDto eventDto, ConstraintValidatorContext constraintValidatorContext) {
        if (eventDto.getEventDate() == null) {
            return true;
        }
        return eventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2));
    }
}
