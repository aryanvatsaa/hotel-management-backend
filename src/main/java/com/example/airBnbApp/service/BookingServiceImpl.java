package com.example.airBnbApp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.modelmapper.ModelMapper;
import com.example.airBnbApp.dto.BookingDto;
import com.example.airBnbApp.dto.BookingRequest;
import com.example.airBnbApp.dto.GuestDto;
import com.example.airBnbApp.entity.Booking;
import com.example.airBnbApp.entity.Guest;
import com.example.airBnbApp.entity.Hotel;
import com.example.airBnbApp.entity.Inventory;
import com.example.airBnbApp.entity.Room;
import com.example.airBnbApp.entity.User;
import com.example.airBnbApp.entity.enums.BookingStatus;
import com.example.airBnbApp.exception.ResourceNotFoundException;
import com.example.airBnbApp.repository.BookingRepository;
import com.example.airBnbApp.repository.HotelRepository;
import com.example.airBnbApp.repository.InventoryRepository;
import com.example.airBnbApp.repository.RoomRepository;
import com.example.airBnbApp.repository.GuestRepository;
import com.example.airBnbApp.exception.UnAuthorisedException;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
@RequiredArgsConstructor 
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    
    @Override
    @Transactional 
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {
        
        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(() ->
            new ResourceNotFoundException("Hotel not found with id : "+bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId()).orElseThrow(() ->
            new ResourceNotFoundException("Room not found with id : "+bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(room.getId(),
            bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()) +1;

        if(inventoryList.size() != daysCount) {
            throw new IllegalStateException("Room is not available anymore");
        }

        // Reserve the room / update the booed count of inventories

        for(Inventory inventory: inventoryList) {
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequest.getRoomsCount());
        }

        inventoryRepository.saveAll(inventoryList);


        // create the booking

        

        // TODO: CALCULATE DYNAMIC PRICING

        Booking booking = Booking.builder()
            .bookingStatus(BookingStatus.RESERVED)
            .hotel(hotel)
            .room(room)
            .checkInDate(bookingRequest.getCheckInDate())
            .checkOutDate(bookingRequest.getCheckOutDate())
            .user(getCurrentUser())
            .roomCount(bookingRequest.getRoomsCount())
            .amount(BigDecimal.TEN)
            .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);


    }

    @Override
    @Transactional 
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
            new ResourceNotFoundException("Booking not found with id : "+bookingId));
        User user = getCurrentUser();

        if (!user.equals(booking.getUser())) {
            throw new UnAuthorisedException("Booking does not belongs to this user with id "+user.getId());
        }

        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already Expried!!");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under Reserved state, cannot add guests!!");
        }

        for(GuestDto guestDto: guestDtoList){
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
   
    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
