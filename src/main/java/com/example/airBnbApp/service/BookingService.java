package com.example.airBnbApp.service;

import java.time.LocalDate;
import java.util.List;

import com.example.airBnbApp.dto.BookingDto;
import com.example.airBnbApp.dto.BookingRequest;
import com.example.airBnbApp.dto.GuestDto;
import com.example.airBnbApp.dto.HotelReportDto;
import com.example.airBnbApp.dto.UserDto;
import com.stripe.model.Event;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);

    
    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayments(Long bookingId);


    void capturePayment(Event event);


    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingsByHotelId(Long hotelId);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getMyBookings();

    UserDto getMyProfile();

}
