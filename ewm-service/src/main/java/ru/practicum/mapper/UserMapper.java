package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.user.UserFullDto;
import ru.practicum.dto.user.UserRequest;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.User;

@Component
public class UserMapper {
    public User toUser(UserRequest userRequest) {
        return User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .build();
    }

    public UserFullDto toFullDto(User user) {
        return UserFullDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public UserShortDto toShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
