package com.example.banking.controller;

import com.example.banking.dto.UserSearchDto;
import com.example.banking.dto.UserUpdateDto;
import com.example.banking.model.User;
import com.example.banking.security.JwtUtil;
import com.example.banking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long currentUserId = jwtUtil.extractUserId(token);
        if (!currentUserId.equals(id)) {
            throw new AccessDeniedException("You can only access your own data");
        }

        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String dateOfBirth,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserSearchDto searchDto = new UserSearchDto(name, dateOfBirth, email, phone);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.searchUsers(searchDto, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateDto updateDto,
            @RequestHeader("Authorization") String token) {

        Long userIdFromToken = jwtUtil.extractUserId(token); // Используем внедренный экземпляр
        if (!id.equals(userIdFromToken)) {
            throw new AccessDeniedException("You can only update your own data");
        }

        return ResponseEntity.ok(userService.updateUserData(id, updateDto));
    }
}