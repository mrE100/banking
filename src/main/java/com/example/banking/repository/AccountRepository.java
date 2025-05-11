package com.example.banking.repository;


import com.example.banking.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.user.id = :userId")
    void updateBalanceByUserId(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance - :amount WHERE a.user.id = :userId AND a.balance >= :amount")
    int withdrawFromAccount(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.user.id = :userId")
    int depositToAccount(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}