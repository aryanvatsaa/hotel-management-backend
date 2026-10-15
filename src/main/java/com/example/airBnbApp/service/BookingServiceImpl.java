package com.example.airBnbApp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.airBnbApp.util.AppUtils.getCurrentUser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import com.example.airBnbApp.strategy.PricingService;
import org.modelmapper.ModelMapper;
import com.example.airBnbApp.dto.BookingDto;
import com.example.airBnbApp.dto.BookingRequest;
import com.example.airBnbApp.dto.GuestDto;
import com.example.airBnbApp.dto.HotelReportDto;
import com.example.airBnbApp.dto.UserDto;
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
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
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
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;
    
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

        inventoryRepository.initBooking(room.getId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());
        
        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

        Booking booking = Booking.builder()
            .bookingStatus(BookingStatus.RESERVED)
            .hotel(hotel)
            .room(room)
            .checkInDate(bookingRequest.getCheckInDate())
            .checkOutDate(bookingRequest.getCheckOutDate())
            .user(getCurrentUser())
            .roomCount(bookingRequest.getRoomsCount())
            .amount(totalPrice)
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
   
    

	@Override
    @Transactional
	public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
            () -> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();

        if (!user.equals(booking.getUser())) {
            throw new UnAuthorisedException("Booking does not belongs to this user with id "+user.getId());
        }
        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already Expried!!");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking, 
            frontendUrl+"/payments/success", frontendUrl+"/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

		return sessionUrl;
	}

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) return;

            String sessionId = session.getId();
            Booking booking =
                    bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(() ->
                            new ResourceNotFoundException("Booking not found for session ID: "+sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomCount());

            log.info("Successfully confirmed the booking for Booking ID: {}", booking.getId());
        } else {
            log.warn("Unhandled event type: {}", event.getType());
        }
    }

	@Override
    @Transactional
	public void cancelBooking(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId).orElseThrow(
            () -> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();

        if (!user.equals(booking.getUser())) {
            throw new UnAuthorisedException("Booking does not belongs to this user with id "+user.getId());
        }
        
        if(booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be cancelled !!");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
            booking.getCheckOutDate(), booking.getRoomCount());

        inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
            booking.getCheckOutDate(), booking.getRoomCount());

        // handle the refund

        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                .setPaymentIntent(session.getPaymentIntent())
                .build();

            Refund.create(refundParams);
                
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }


	}

	@Override
	public String getBookingStatus(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId).orElseThrow(
            () -> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();

        if (!user.equals(booking.getUser())) {
            throw new UnAuthorisedException("Booking does not belongs to this user with id "+user.getId());
        }

        return booking.getBookingStatus().name();
	}

	@Override
	public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {
		Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel Not found with ID: "+hotelId));
        
        User user = getCurrentUser();

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel !!");

        List<Booking> bookings = bookingRepository.findByHotel(hotel);

        return bookings.stream().map((element) -> modelMapper.map(element, BookingDto.class)).collect(Collectors.toList());
	}

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel Not found with ID: "+hotelId));
        
        User user = getCurrentUser();

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel !!");

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);

        Long totalConfirmedBookings = bookings.stream().filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED).count();

        BigDecimal totalRevenueOfConfirmedBookings = bookings.stream()
            .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
            .map(Booking::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBookings == 0 ? BigDecimal.ZERO : totalRevenueOfConfirmedBookings
            .divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportDto(totalConfirmedBookings, totalRevenueOfConfirmedBookings, avgRevenue);
    }

    @Override
    public List<BookingDto> getMyBookings() {

        User user = getCurrentUser();

        return bookingRepository.findByUser(user)
            .stream()
            .map((element) -> modelMapper.map(element, BookingDto.class))
            .collect(Collectors.toList());

    }

    @Override
    public UserDto getMyProfile() {
        return modelMapper.map(getCurrentUser(), UserDto.class);
    }
}
