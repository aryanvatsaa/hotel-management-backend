package com.example.airBnbApp.dto;

import com.example.airBnbApp.entity.User;
import com.example.airBnbApp.entity.enums.Gender;
import lombok.Data;

@Data 
public class GuestDto {
    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;
}
