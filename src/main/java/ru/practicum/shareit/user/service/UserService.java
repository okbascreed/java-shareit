package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto updateUser(long userId, UserDto userDto);

    List<UserDto> getAllUsers();

    UserDto getUserById(Long userId);

    void deleteUser(Long userId);
}
