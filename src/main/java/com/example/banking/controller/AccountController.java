package com.example.banking.controller;

import com.example.banking.dto.TransferRequestDto;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.ConcurrentTransactionException;
import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.security.JwtUtil;
import com.example.banking.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final JwtUtil jwtUtil;  // Добавляем JwtUtil как зависимость

    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(
            @RequestBody @Valid TransferRequestDto transferDto,
            @RequestHeader("Authorization") String token)
            throws AccountNotFoundException, InsufficientFundsException, ConcurrentTransactionException {

        Long fromUserId = jwtUtil.extractUserId(token);  // Вызываем метод через экземпляр
        accountService.transferMoney(fromUserId, transferDto.getToUserId(), transferDto.getAmount());
        return ResponseEntity.ok().build();
    }
}