package ru.practicum.ewm.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.service.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserServiceImpl userService;

    @Test
    void createUser_ShouldReturnDto() {
        NewUserRequest request = new NewUserRequest();
        request.setName("test");
        request.setEmail("test@example.com");
        User user = new User(1L, "test", "test@example.com");

        when(userRepository.save(any())).thenReturn(user);

        UserDto result = userService.createUser(request);

        assertEquals(1L, result.getId());
        assertEquals("test", result.getName());
    }

    @Test
    void getUsers_WithIds_ShouldReturnFiltered() {
        List<Long> ids = List.of(1L, 2L);
        List<User> users = List.of(new User(1L, "u1", "e1@mail.ru"));
        when(userRepository.findAllById(ids)).thenReturn(users);

        List<UserDto> result = userService.getUsers(ids, 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void deleteUser_WhenExists_ShouldDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WhenNotFound_ShouldThrow() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(99L));
    }
}