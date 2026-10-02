package com.example.airBnbApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.airBnbApp.dto.BookingDto;
import com.example.airBnbApp.dto.BookingRequest;
import com.example.airBnbApp.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor 
@RequestMapping("/bookings")
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init") 
    public ResponseEntity<BookingDto> initialiseBooking(@RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));
    }
}
