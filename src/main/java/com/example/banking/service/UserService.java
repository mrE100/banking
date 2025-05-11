package com.example.banking.service;

import com.example.banking.dto.UserSearchDto;
import com.example.banking.dto.UserUpdateDto;
import com.example.banking.exception.DuplicateEmailException;
import com.example.banking.exception.DuplicatePhoneException;
import com.example.banking.exception.UserNotFoundException;
import com.example.banking.model.EmailData;
import com.example.banking.model.PhoneData;
import com.example.banking.model.User;
import com.example.banking.repository.EmailDataRepository;
import com.example.banking.repository.PhoneDataRepository;
import com.example.banking.repository.UserRepository;
import com.example.banking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final EmailDataRepository emailDataRepository;
    private final PhoneDataRepository phoneDataRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with name: " + name));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getName())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }

    @CacheEvict(value = "users", key = "#userId")
    @Transactional
    public User updateUserData(Long userId, UserUpdateDto updateDto) {
        User user = getUser(userId);

        if (updateDto.getName() != null) {
            user.setName(updateDto.getName());
        }

        if (updateDto.getDateOfBirth() != null) {
            user.setDateOfBirth(updateDto.getDateOfBirth());
        }

        if (updateDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        if (updateDto.getEmails() != null) {
            processEmailUpdates(user, updateDto.getEmails());
        }

        if (updateDto.getPhones() != null) {
            processPhoneUpdates(user, updateDto.getPhones());
        }

        return userRepository.save(user);
    }

    private void processEmailUpdates(User user, Set<UserUpdateDto.EmailData> emailUpdates) {
        Set<EmailData> emailsToKeep = new HashSet<>();

        for (UserUpdateDto.EmailData emailData : emailUpdates) {
            if (emailData.getId() != null) {
                EmailData existingEmail = user.getEmails().stream()
                        .filter(e -> e.getId().equals(emailData.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Email not found with id: " + emailData.getId()));

                if (!existingEmail.getEmail().equals(emailData.getEmail())) {
                    validateEmailUniqueness(emailData.getEmail());
                    existingEmail.setEmail(emailData.getEmail());
                }
                emailsToKeep.add(existingEmail);
            } else {
                validateEmailUniqueness(emailData.getEmail());
                EmailData newEmail = new EmailData();
                newEmail.setEmail(emailData.getEmail());
                newEmail.setUser(user);
                emailsToKeep.add(newEmail);
            }
        }

        user.getEmails().removeIf(email -> !emailsToKeep.contains(email));
        user.getEmails().addAll(emailsToKeep);
    }

    private void processPhoneUpdates(User user, Set<UserUpdateDto.PhoneData> phoneUpdates) {
        Set<PhoneData> phonesToKeep = new HashSet<>();

        for (UserUpdateDto.PhoneData phoneData : phoneUpdates) {
            if (phoneData.getId() != null) {
                PhoneData existingPhone = user.getPhones().stream()
                        .filter(p -> p.getId().equals(phoneData.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Phone not found with id: " + phoneData.getId()));

                if (!existingPhone.getPhone().equals(phoneData.getPhone())) {
                    validatePhoneUniqueness(phoneData.getPhone());
                    existingPhone.setPhone(phoneData.getPhone());
                }
                phonesToKeep.add(existingPhone);
            } else {
                validatePhoneUniqueness(phoneData.getPhone());
                PhoneData newPhone = new PhoneData();
                newPhone.setPhone(phoneData.getPhone());
                newPhone.setUser(user);
                phonesToKeep.add(newPhone);
            }
        }

        user.getPhones().removeIf(phone -> !phonesToKeep.contains(phone));
        user.getPhones().addAll(phonesToKeep);
    }

    private void validateEmailUniqueness(String email) {
        if (emailDataRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already exists: " + email);
        }
    }

    private void validatePhoneUniqueness(String phone) {
        if (phoneDataRepository.existsByPhone(phone)) {
            throw new DuplicatePhoneException("Phone already exists: " + phone);
        }
    }

    @Cacheable(value = "users", key = "#userId")
    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsers(UserSearchDto searchDto, Pageable pageable) {
        return userRepository.searchUsers(
                searchDto.getName(),
                searchDto.getDateOfBirth(),
                searchDto.getEmail(),
                searchDto.getPhone(),
                pageable
        );
    }

    public String generateTokenForUser(Long userId) {
        User user = getUser(userId);
        return jwtUtil.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getName())
                        .password("")
                        .authorities("USER")
                        .build()
        );
    }
}