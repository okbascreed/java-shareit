package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@AllArgsConstructor
public class ItemDto {
    long id;

    @Size(min = 1, max = 30)
    @NotEmpty
    String name;

    @Size(min = 1, max = 250)
    @NotEmpty
    String description;

    @NotNull
    Boolean available;

    ItemRequest request;
}
