package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long userId = 0L;

    @Override
    public User createUser(User user) {
        user.setId(++userId);
        users.put(user.getId(), user);
        return user;
    }
    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }
    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }
    @Override
    public User getUserById(Long userId) {
        return users.get(userId);
    }
    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
