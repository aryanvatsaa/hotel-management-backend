package com.example.airBnbApp.dto;

import java.time.LocalDate;

import lombok.Data;

@Data 
public class BookingRequest {
    private Long HotelId;
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer roomsCount;

}
