package com.example.airBnbApp.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.airBnbApp.dto.HotelDto;
import com.example.airBnbApp.dto.RoomDto;
import com.example.airBnbApp.dto.HotelInfoDto;
import com.example.airBnbApp.entity.Hotel;
import com.example.airBnbApp.entity.User;
import com.example.airBnbApp.entity.Room;
import com.example.airBnbApp.exception.ResourceNotFoundException;
import com.example.airBnbApp.exception.UnAuthorisedException;
import com.example.airBnbApp.repository.HotelRepository;
import com.example.airBnbApp.repository.RoomRepository;

import jakarta.transaction.Transactional;

import java.util.List;

import org.modelmapper.ModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor 
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
	private final ModelMapper modelMapper;
	private final InventoryService inventoryService;
	private final RoomRepository roomRepository;
    
    @Override
	public HotelDto createNewHotel(HotelDto hotelDto) {
		log.info("Creating a new hotel with name : {}", hotelDto.getName());
		Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
		hotel.setActive(false);

		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		hotel.setOwner(user);

		hotel = hotelRepository.save(hotel);
		log.info("Created a new hotel with ID : {}", hotelDto.getId());
		return modelMapper.map(hotel, HotelDto.class);
	}

	@Override
	public HotelDto getHotelById(Long id) {
		log.info("Getting the hotel with ID : {}", id);
		Hotel hotel = hotelRepository
			.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID : "+id));
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if(!user.equals(hotel.getOwner())){
			throw new UnAuthorisedException("This user does not owns this hotel with ID "+id);
		}
		return modelMapper.map(hotel, HotelDto.class);

	
	}

	@Override
	public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
		log.info("Updating the hotel with ID : {}", id);
		Hotel hotel = hotelRepository
			.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID : "+id));

		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!user.equals(hotel.getOwner())){
			throw new UnAuthorisedException("This user does not owns this hotel with ID "+id);
		}

		modelMapper.map(hotelDto, hotel);
		hotel.setId(id);
		hotel = hotelRepository.save(hotel);
		return modelMapper.map(hotel, HotelDto.class);
	}

	@Override
	@Transactional
	public void deleteHotelById(Long id) {
		Hotel hotel = hotelRepository
			.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID : "+id));

		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!user.equals(hotel.getOwner())){
			throw new UnAuthorisedException("This user does not owns this hotel with ID "+id);
		}

		
		for(Room room: hotel.getRooms()) {
			inventoryService.deleteAllInventories(room);
			roomRepository.deleteById(room.getId());
		}
		hotelRepository.deleteById(id);
	}

	@Override
	@Transactional
	public void activateHotel(Long hotelId) {
		log.info("Activating the hotel with ID : {}", hotelId);
		Hotel hotel = hotelRepository
			.findById(hotelId)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID : "+hotelId));

		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(!user.equals(hotel.getOwner())){
			throw new UnAuthorisedException("This user does not owns this hotel with ID "+hotelId);
		}
		
		hotel.setActive(true);

        //assuming only do it now
		for(Room room: hotel.getRooms()) {
			inventoryService.initializeRoomForAYear(room);
		}

		
	}

	@Override
	public HotelInfoDto getHotelInfoById(Long hotelId) {
		Hotel hotel = hotelRepository
			.findById(hotelId)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID : "+hotelId));


		List<RoomDto> rooms = hotel.getRooms()
			.stream()
			.map((element) -> modelMapper.map(element, RoomDto.class))
			.toList();
			
		return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
	}


	
}