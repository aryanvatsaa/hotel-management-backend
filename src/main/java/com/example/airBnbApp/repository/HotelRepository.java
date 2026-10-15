package com.example.airBnbApp.repository;

import com.example.airBnbApp.entity.Hotel;
import com.example.airBnbApp.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByOwner(User user);

}
