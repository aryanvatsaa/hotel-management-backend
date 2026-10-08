package com.example.airBnbApp.repository;

import com.example.airBnbApp.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByEmail(String email);

}
