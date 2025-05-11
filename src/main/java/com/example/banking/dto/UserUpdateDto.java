package com.example.banking.dto;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.Set;

public class UserUpdateDto {

    @Size(min = 1, max = 500, message = "Name must be between 1 and 500 characters")
    private String name;

    @Pattern(regexp = "^(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.(19|20)\\d\\d$",
            message = "Date of birth must be in format DD.MM.YYYY")
    private String dateOfBirth;

    @Size(min = 8, max = 500, message = "Password must be between 8 and 500 characters")
    private String password;

    @NotNull(message = "At least one email must be provided")
    @Size(min = 1, message = "At least one email must be provided")
    private Set<@Valid EmailData> emails;

    @NotNull(message = "At least one phone must be provided")
    @Size(min = 1, message = "At least one phone must be provided")
    private Set<@Valid PhoneData> phones;

    // Static inner classes for email and phone data
    public static class EmailData {
        private Long id;

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email cannot be empty")
        @Size(max = 200, message = "Email must be less than 200 characters")
        private String email;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class PhoneData {
        private Long id;

        @Pattern(regexp = "^\\d{11}$", message = "Phone must be 11 digits")
        @NotBlank(message = "Phone cannot be empty")
        private String phone;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<EmailData> getEmails() { return emails; }
    public void setEmails(Set<EmailData> emails) { this.emails = emails; }
    public Set<PhoneData> getPhones() { return phones; }
    public void setPhones(Set<PhoneData> phones) { this.phones = phones; }
}
