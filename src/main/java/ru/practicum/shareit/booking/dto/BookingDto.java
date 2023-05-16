package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.enums.BookingStatus;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    @Min(value = 0, message = "ID BookingDto can't be negative.")
    private Long id;

    @Future
    private LocalDateTime start;
    @Future
    private LocalDateTime end;

    @Min(value = 0, message = "ItemID can't be negative.")
    private Long itemId;

    @Min(value = 0, message = "BookerID can't be negative.")
    private Long bookerId;

    private BookingStatus status;

}

