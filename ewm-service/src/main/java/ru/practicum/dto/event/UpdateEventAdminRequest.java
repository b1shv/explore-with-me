package ru.practicum.dto.event;

import ru.practicum.model.state.AdminStateAction;

import java.util.Objects;

public class UpdateEventAdminRequest extends EventDto {
    private AdminStateAction stateAction;

    public AdminStateAction getStateAction() {
        return stateAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UpdateEventAdminRequest that = (UpdateEventAdminRequest) o;
        return getStateAction() == that.getStateAction();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getStateAction());
    }
}
