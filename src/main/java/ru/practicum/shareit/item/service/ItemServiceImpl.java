package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        Item item = ItemMapper.mapToItem(itemDto);
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с таким id не найден.");
        }
        item.setOwner(userStorage.getUserById(userId));
        itemStorage.createItem(item);
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(long userId) {
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с таким id не найден.");
        }
        return itemStorage.getAllItems()
                .stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с таким id не найден.");
        }
        Item item = itemStorage.getItemById(itemId);
        if (itemStorage.getItemById(itemId).getOwner().getId().equals(userId)) {
            if (itemDto.getName() != null) {
                item.setName(itemDto.getName());
            }
            if (itemDto.getDescription() != null) {
                item.setDescription(itemDto.getDescription());
            }
            if (itemDto.getAvailable() != null) {
                item.setAvailable(itemDto.getAvailable());
            }
            if (itemDto.getRequest() != null) {
                item.setRequest(itemDto.getRequest());
            }
        } else {
            throw new NotFoundException("Id пользователя создавшего вещь не совпадает с Вашим.");
        }
        itemStorage.updateItem(item);
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemStorage.getItemById(itemId);
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public List<ItemDto> search(String text) {
        List<Item> searchItems = new ArrayList<>();

        if (text.isEmpty()) {
            return new ArrayList<>();
        }

        for (Item item : itemStorage.getAllItems()) {
            boolean condition = item.getName().toLowerCase().contains(text.toLowerCase()) ||
                    item.getDescription().toLowerCase().contains(text.toLowerCase());
            if (condition && item.isAvailable()) {
                searchItems.add(item);
            }
        }
        return searchItems.stream().map(ItemMapper::mapToItemDto).collect(Collectors.toList());
    }
}
