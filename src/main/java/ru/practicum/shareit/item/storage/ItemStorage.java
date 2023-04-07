package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item createItem(Item item);

    List<Item> getAllItems();

    Item updateItem(Item item);

    Item getItemById(Long itemId);
}
