package ru.practicum.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.model.User;

import java.util.List;

public interface UserService {
    List<User> getUsers(List<Long> ids, Pageable pageable);

    User getUserById(long id);

    User addUser(User user);

    void deleteUser(long id);
}
