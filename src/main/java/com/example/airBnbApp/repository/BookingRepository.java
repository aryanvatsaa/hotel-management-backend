package com.example.airBnbApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.airBnbApp.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPaymentSessionId(String sessionId);

}
