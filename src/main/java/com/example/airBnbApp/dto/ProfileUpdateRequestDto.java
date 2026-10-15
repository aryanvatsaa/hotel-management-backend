package com.example.airBnbApp.dto;

import java.time.LocalDate;

import com.example.airBnbApp.entity.enums.Gender;

import lombok.Data;

@Data 
public class ProfileUpdateRequestDto {

    private String name;
    private LocalDate dateOfBirth;
    private Gender gender; 
    
}
