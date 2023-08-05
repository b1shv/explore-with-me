package ru.practicum.dto.event;

import ru.practicum.model.state.UserStateAction;

import java.util.Objects;

public class UpdateEventUserRequest extends EventDto {
    private UserStateAction stateAction;

    public UserStateAction getStateAction() {
        return stateAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        UpdateEventUserRequest that = (UpdateEventUserRequest) o;
        return getStateAction() == that.getStateAction();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getStateAction());
    }
}
