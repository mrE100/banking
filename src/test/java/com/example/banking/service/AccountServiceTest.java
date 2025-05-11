package com.example.banking.service;

import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.model.Account;
import com.example.banking.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RedisTemplate<String, Boolean> redisTemplate;

    @Mock
    private ValueOperations<String, Boolean> valueOperations;

    @InjectMocks
    private AccountService accountService;

    @Test
    void transferMoney_Success() throws Exception {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq(true), any(Duration.class)))
                .thenReturn(true);

        when(accountRepository.withdrawFromAccount(eq(1L), any(BigDecimal.class)))
                .thenReturn(1);
        when(accountRepository.depositToAccount(eq(2L), any(BigDecimal.class)))
                .thenReturn(1);

        // Act
        accountService.transferMoney(1L, 2L, new BigDecimal("100"));

        // Assert
        verify(accountRepository).withdrawFromAccount(1L, new BigDecimal("100"));
        verify(accountRepository).depositToAccount(2L, new BigDecimal("100"));
        verify(redisTemplate).delete("transfer:1:2");
    }

    @Test
    void transferMoney_InsufficientFunds() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq(true), any(Duration.class)))
                .thenReturn(true);
        when(accountRepository.withdrawFromAccount(anyLong(), any(BigDecimal.class)))
                .thenReturn(0);

        // Act & Assert
        assertThrows(InsufficientFundsException.class, () ->
                accountService.transferMoney(1L, 2L, new BigDecimal("100")));

        verify(redisTemplate).delete(anyString());
    }

    @Test
    void applyInterest_ShouldCapAt207Percent() {
        // Arrange
        Account account = new Account();
        account.setBalance(new BigDecimal("200"));
        account.setInitialDeposit(new BigDecimal("100"));

        when(accountRepository.findAll()).thenReturn(List.of(account));

        // Act
        accountService.applyInterest();

        // Assert
        assertEquals(new BigDecimal("207.00"), account.getBalance());
        verify(accountRepository).save(account);
    }
}