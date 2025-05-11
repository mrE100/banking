package com.example.banking.repository;

import com.example.banking.model.PhoneData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {

    // Проверка существования телефона
    boolean existsByPhone(String phone);

    // Поиск по номеру телефона
    Optional<PhoneData> findByPhone(String phone);

    // Поиск всех телефонов пользователя
    @Query("SELECT p FROM PhoneData p WHERE p.user.id = :userId")
    List<PhoneData> findAllByUserId(@Param("userId") Long userId);

    // Удаление телефона пользователя
    @Modifying
    @Query("DELETE FROM PhoneData p WHERE p.id = :phoneId AND p.user.id = :userId")
    int deleteUserPhone(@Param("userId") Long userId, @Param("phoneId") Long phoneId);

    // Проверка принадлежности телефона пользователю
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM PhoneData p WHERE p.id = :phoneId AND p.user.id = :userId")
    boolean isPhoneBelongsToUser(@Param("userId") Long userId, @Param("phoneId") Long phoneId);
}