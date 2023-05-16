package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Builder
@Data
public class CommentDto {
    private Long id;
    @Size(min = 0, max = 1024)
    private String text;
    private String authorName;
    private LocalDateTime created;
}
