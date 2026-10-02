package com.example.airBnbApp.service;

import com.example.airBnbApp.dto.BookingDto;
import com.example.airBnbApp.dto.BookingRequest;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);

}
