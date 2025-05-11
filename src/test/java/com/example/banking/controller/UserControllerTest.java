package com.example.banking.controller;

import com.example.banking.dto.UserSearchDto;
import com.example.banking.dto.UserUpdateDto;
import com.example.banking.model.User;
import com.example.banking.security.JwtUtil;
import com.example.banking.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserController userController;

    private static final Long USER_ID = 1L;
    private static final String VALID_TOKEN = "Bearer valid.token.here";
    private static final String INVALID_TOKEN = "Bearer invalid.token.here";
    private static final Long OTHER_USER_ID = 2L;

    private User testUser;
    private UserUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setName("Test User");
        testUser.setDateOfBirth("01.01.1990");
        testUser.setPassword("password123");

        updateDto = new UserUpdateDto();
        updateDto.setName("Updated Name");
        updateDto.setDateOfBirth("01.01.1990");
        updateDto.setPassword("newPassword123");

        UserUpdateDto.EmailData emailData = new UserUpdateDto.EmailData();
        emailData.setEmail("test@example.com");
        updateDto.setEmails(Set.of(emailData));

        UserUpdateDto.PhoneData phoneData = new UserUpdateDto.PhoneData();
        phoneData.setPhone("12345678901");
        updateDto.setPhones(Set.of(phoneData));
    }

    @Test
    void getUser_WithValidTokenAndOwnId_ReturnsUser() {
        // Arrange
        when(jwtUtil.extractUserId(VALID_TOKEN)).thenReturn(USER_ID);
        when(userService.getUser(USER_ID)).thenReturn(testUser);

        // Act
        ResponseEntity<User> response = userController.getUser(USER_ID, VALID_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(jwtUtil).extractUserId(VALID_TOKEN);
        verify(userService).getUser(USER_ID);
    }

    @Test
    void getUser_WithInvalidToken_ThrowsAccessDeniedException() {
        // Arrange
        when(jwtUtil.extractUserId(INVALID_TOKEN)).thenReturn(OTHER_USER_ID);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            userController.getUser(USER_ID, INVALID_TOKEN);
        });

        verify(jwtUtil).extractUserId(INVALID_TOKEN);
        verifyNoInteractions(userService);
    }

    @Test
    void searchUsers_WithAllParameters_ReturnsPageOfUsers() {
        // Arrange
        String name = "Test";
        String dateOfBirth = "01.01.1990";
        String email = "test@example.com";
        String phone = "12345678901";
        int page = 0;
        int size = 10;

        Pageable pageable = PageRequest.of(page, size);
        Page<User> expectedPage = new PageImpl<>(List.of(testUser));

        when(userService.searchUsers(any(UserSearchDto.class), eq(pageable))).thenReturn(expectedPage);

        // Act
        ResponseEntity<Page<User>> response = userController.searchUsers(
                name, dateOfBirth, email, phone, page, size);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
        verify(userService).searchUsers(any(UserSearchDto.class), eq(pageable));
    }

    @Test
    void searchUsers_WithNoParameters_ReturnsPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> expectedPage = new PageImpl<>(Collections.emptyList());

        when(userService.searchUsers(any(UserSearchDto.class), eq(pageable))).thenReturn(expectedPage);

        // Act
        ResponseEntity<Page<User>> response = userController.searchUsers(
                null, null, null, null, 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPage, response.getBody());
        verify(userService).searchUsers(any(UserSearchDto.class), eq(pageable));
    }

    @Test
    void updateUser_WithValidTokenAndOwnId_UpdatesUser() {
        // Arrange
        when(jwtUtil.extractUserId(VALID_TOKEN)).thenReturn(USER_ID);
        when(userService.updateUserData(eq(USER_ID), any(UserUpdateDto.class))).thenReturn(testUser);

        // Act
        ResponseEntity<User> response = userController.updateUser(USER_ID, updateDto, VALID_TOKEN);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(jwtUtil).extractUserId(VALID_TOKEN);
        verify(userService).updateUserData(eq(USER_ID), any(UserUpdateDto.class));
    }

    @Test
    void updateUser_WithInvalidToken_ThrowsAccessDeniedException() {
        // Arrange
        when(jwtUtil.extractUserId(INVALID_TOKEN)).thenReturn(OTHER_USER_ID);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            userController.updateUser(USER_ID, updateDto, INVALID_TOKEN);
        });

        verify(jwtUtil).extractUserId(INVALID_TOKEN);
        verifyNoInteractions(userService);
    }

    @Test
    void updateUser_WithInvalidData_ShouldFailValidation() {
        // Arrange
        UserUpdateDto invalidDto = new UserUpdateDto();
        invalidDto.setName("");
        invalidDto.setDateOfBirth("1990-01-01");
        invalidDto.setPassword("short");
        invalidDto.setEmails(Collections.emptySet());
        invalidDto.setPhones(Collections.emptySet());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserUpdateDto>> violations = validator.validate(invalidDto);

        // Assert
        assertFalse(violations.isEmpty());
        // Проверяем, что есть нарушения для каждого невалидного поля
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("emails")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phones")));
    }

    @Test
    void searchUsers_WithDateOfBirthFilter_ReturnsUsersBornAfterGivenDate() {
        // Arrange
        String dateOfBirth = "01.01.1990";
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User(); // Должен быть найден (родился позже)
        user1.setId(1L);
        user1.setName("User 1");
        user1.setDateOfBirth("02.01.1990"); // Дата позже, чем 01.01.1990

        User user2 = new User(); // Не должен быть найден (родился раньше)
        user2.setId(2L);
        user2.setName("User 2");
        user2.setDateOfBirth("31.12.1989"); // Дата раньше, чем 01.01.1990

        // Ожидаемый результат — только user1
        Page<User> expectedPage = new PageImpl<>(List.of(user1));

        when(userService.searchUsers(
                any(UserSearchDto.class),
                eq(pageable)
        )).thenReturn(expectedPage);

        // Act
        ResponseEntity<Page<User>> response = userController.searchUsers(
                null, dateOfBirth, null, null, 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("02.01.1990", response.getBody().getContent().get(0).getDateOfBirth());
    }
}