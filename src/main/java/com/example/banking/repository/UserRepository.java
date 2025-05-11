package com.example.banking.repository;

import com.example.banking.model.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Cacheable("users")
    Optional<User> findById(Long id);

    @Cacheable("usersByName")
    Optional<User> findByName(String name); // Используем name вместо username

    @Query("SELECT u FROM User u WHERE " +
            "(:name IS NULL OR u.name LIKE %:name%) AND " +
            "(:dateOfBirth IS NULL OR u.dateOfBirth > :dateOfBirth) AND " +
            "(:email IS NULL OR EXISTS (SELECT 1 FROM u.emails e WHERE e.email = :email)) AND " +
            "(:phone IS NULL OR EXISTS (SELECT 1 FROM u.phones p WHERE p.phone = :phone))")
    Page<User> searchUsers(
            @Param("name") String name,
            @Param("dateOfBirth") String dateOfBirth,
            @Param("email") String email,
            @Param("phone") String phone,
            Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u JOIN u.emails e WHERE e.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u JOIN u.phones p WHERE p.phone = :phone")
    boolean existsByPhone(@Param("phone") String phone);
}