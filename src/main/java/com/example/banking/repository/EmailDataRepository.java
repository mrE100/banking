package com.example.banking.repository;

import com.example.banking.model.EmailData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailDataRepository extends JpaRepository<EmailData, Long> {

    // Проверка существования email
    boolean existsByEmail(String email);

    // Поиск по email
    Optional<EmailData> findByEmail(String email);

    // Поиск всех email пользователя
    @Query("SELECT e FROM EmailData e WHERE e.user.id = :userId")
    List<EmailData> findAllByUserId(@Param("userId") Long userId);

    // Удаление email пользователя
    @Modifying
    @Query("DELETE FROM EmailData e WHERE e.id = :emailId AND e.user.id = :userId")
    int deleteUserEmail(@Param("userId") Long userId, @Param("emailId") Long emailId);

    // Проверка принадлежности email пользователю
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM EmailData e WHERE e.id = :emailId AND e.user.id = :userId")
    boolean isEmailBelongsToUser(@Param("userId") Long userId, @Param("emailId") Long emailId);
}