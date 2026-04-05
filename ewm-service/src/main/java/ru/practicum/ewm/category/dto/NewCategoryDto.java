package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewCategoryDto {
    @NotBlank(message = "Название категории не может быть пустым")
    private String name;
}