package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserValidationException;
import ru.practicum.shareit.exception.EmailDuplicateException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userEmailDuplicationCheck(userDto);
        User user = userStorage.createUser(UserMapper.mapToUser(userDto));
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public UserDto updateUser(long userId, UserDto userDto) {
        User user = userStorage.getUserById(userId);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (user.getEmail().equals(userDto.getEmail())) {
                user.setEmail(userDto.getEmail());
            } else {
                userEmailDuplicationCheck(userDto);
                user.setEmail(userDto.getEmail());
            }
        }
        userStorage.updateUser(user);
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> usersList = userStorage.getAllUsers();
        List<UserDto> usersDtoList = new ArrayList<>();
        for (User user : usersList) {
            usersDtoList.add(UserMapper.mapToUserDto(user));
        }
        return usersDtoList;
    }

    @Override
    public UserDto getUserById(Long userId) {
        return UserMapper.mapToUserDto(userStorage.getUserById(userId));
    }

    @Override
    public void deleteUser(Long userId) {
        userStorage.deleteUser(userId);
    }

    private void userEmailDuplicationCheck(UserDto userDto) {
        if(userDto.getEmail() == null) {
            throw new UserValidationException("Электронная почта не может быть null!");
        }
        if (userStorage.getAllUsers()
                .stream()
                .anyMatch(user -> user.getEmail().equals(userDto.getEmail()))) {
            throw new EmailDuplicateException("Такая электронная почта уже используется!");
        }
    }
}
