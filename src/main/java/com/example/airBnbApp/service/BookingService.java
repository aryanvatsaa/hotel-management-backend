package com.example.airBnbApp.service;

import java.util.List;

import com.example.airBnbApp.dto.BookingDto;
import com.example.airBnbApp.dto.BookingRequest;
import com.example.airBnbApp.dto.GuestDto;
import com.stripe.model.Event;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);

    
    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayments(Long bookingId);


    void capturePayment(Event event);


    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);

}
