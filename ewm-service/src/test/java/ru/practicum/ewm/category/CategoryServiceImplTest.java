package ru.practicum.ewm.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.category.service.CategoryServiceImpl;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryServiceImpl categoryService;

    @Test
    void createCategory_ShouldReturnDto() {
        NewCategoryDto newDto = new NewCategoryDto();
        newDto.setName("Концерты");
        Category category = new Category(1L, "Концерты");

        when(categoryRepository.save(any())).thenReturn(category);

        CategoryDto result = categoryService.createCategory(newDto);

        assertEquals(1L, result.getId());
        assertEquals("Концерты", result.getName());
    }

    @Test
    void updateCategory_WhenExists_ShouldUpdate() {
        CategoryDto updateDto = new CategoryDto(1L, "Обновлено");
        Category category = new Category(1L, "Старое");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenReturn(category);

        CategoryDto result = categoryService.updateCategory(1L, updateDto);

        assertEquals("Обновлено", result.getName());
    }

    @Test
    void updateCategory_WhenNotFound_ShouldThrow() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> categoryService.updateCategory(99L, new CategoryDto()));
    }

    @Test
    void getCategories_ShouldReturnList() {
        Page<Category> page = new PageImpl<>(List.of(new Category(1L, "Кат1")));
        when(categoryRepository.findAll(any(PageRequest.class))).thenReturn(page);

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertEquals(1, result.size());
    }
}