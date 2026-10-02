package com.example.airBnbApp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import com.example.airBnbApp.entity.enums.BookingStatus;

import lombok.Data;


@Data 
public class BookingDto {
    private Long id;
    private Integer roomCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;
}
