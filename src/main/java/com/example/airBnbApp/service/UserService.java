package com.example.airBnbApp.service;

import com.example.airBnbApp.dto.ProfileUpdateRequestDto;
import com.example.airBnbApp.entity.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);
}
