package com.example.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSearchDto {
    private String name;
    private String dateOfBirth;
    private String email;
    private String phone;
}
