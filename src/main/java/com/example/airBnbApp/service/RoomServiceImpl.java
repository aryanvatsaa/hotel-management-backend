package com.example.airBnbApp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.airBnbApp.entity.Hotel;
import com.example.airBnbApp.entity.Room;
import com.example.airBnbApp.dto.RoomDto;
import com.example.airBnbApp.repository.RoomRepository;

import jakarta.transaction.Transactional;

import com.example.airBnbApp.repository.HotelRepository;
import com.example.airBnbApp.exception.ResourceNotFoundException;
import org.modelmapper.ModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;

    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new room in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
			.findById(hotelId)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID : "+hotelId));
        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        if (hotel.getActive()) {
            inventoryService.initializeRoomForAYear(room);
        }


        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    @Transactional 
    public void deleteRoomById(Long roomId) {
        log.info("Deleting room with ID: {}", roomId);
        Room room = roomRepository
            .findById(roomId)
			.orElseThrow(() -> new ResourceNotFoundException("Room not found with ID : "+roomId));
        inventoryService.deleteFutureInventories(room);

        roomRepository.deleteById(roomId);

    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all room in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
			.findById(hotelId)
			.orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID : "+hotelId));

            return hotel.getRooms()
                .stream()
                .map((element)-> modelMapper.map(element, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room with ID: {}", roomId);
        Room room = roomRepository
            .findById(roomId)
			.orElseThrow(() -> new ResourceNotFoundException("Room not found with ID : "+roomId));
        return modelMapper.map(room, RoomDto.class);
    }   

}
