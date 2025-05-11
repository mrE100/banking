package com.example.banking.service;

import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.ConcurrentTransactionException;
import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.model.Account;
import com.example.banking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final RedisTemplate<String, Boolean> redisTemplate;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void applyInterest() {
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            BigDecimal maxAllowed = account.getInitialDeposit().multiply(new BigDecimal("2.07"));
            BigDecimal newBalance = account.getBalance().multiply(new BigDecimal("1.1"));

            if (newBalance.compareTo(maxAllowed) > 0) {
                newBalance = maxAllowed;
            }

            account.setBalance(newBalance);
            accountRepository.save(account);
        }
    }

    @Transactional
    public void transferMoney(Long fromUserId, Long toUserId, BigDecimal amount)
            throws AccountNotFoundException, InsufficientFundsException, ConcurrentTransactionException {
        String lockKey = String.format("transfer:%s:%s", fromUserId, toUserId);

        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, true, Duration.ofSeconds(30));
            if (locked == null || !locked) {
                throw new ConcurrentTransactionException("Transaction in progress");
            }

            int withdrawResult = accountRepository.withdrawFromAccount(fromUserId, amount);
            if (withdrawResult == 0) {
                throw new InsufficientFundsException("Not enough balance");
            }

            int depositResult = accountRepository.depositToAccount(toUserId, amount);
            if (depositResult == 0) {
                throw new AccountNotFoundException("Recipient account not found");
            }

        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}