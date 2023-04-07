package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    List<ItemDto> getAllItemsByUserId(long userId);

    ItemDto updateItem(long userId, long itemId, ItemDto itemDto);

    ItemDto getItemById(Long itemId);

    List<ItemDto> search(String text);
}
