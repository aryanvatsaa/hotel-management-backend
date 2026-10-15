package com.example.airBnbApp.service;
import com.example.airBnbApp.dto.HotelPriceDto;
import com.example.airBnbApp.dto.HotelSearchRequest;
import com.example.airBnbApp.dto.InventoryDto;
import com.example.airBnbApp.dto.UpdateInventoryRequestDto;
import com.example.airBnbApp.entity.Inventory;
import com.example.airBnbApp.entity.Room;
import com.example.airBnbApp.entity.User;
import com.example.airBnbApp.exception.ResourceNotFoundException;
import com.example.airBnbApp.repository.InventoryRepository;
import com.example.airBnbApp.repository.RoomRepository;
import com.example.airBnbApp.repository.HotelMinPriceRepository;

import static com.example.airBnbApp.util.AppUtils.getCurrentUser;

import java.math.BigDecimal;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@RequiredArgsConstructor 
@Slf4j 
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository ;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final RoomRepository roomRepository;
    
    private final ModelMapper modelMapper;

	@Override
	public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for (; !today.isAfter(endDate); today = today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }
		
	}

	@Override
	public void deleteAllInventories(Room room) {
		inventoryRepository.deleteByRoom(room);
	}

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());
        long dateCount = 
            ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate()) +1 ;


        // business logic - 90 days
        Page<HotelPriceDto> hotelPage = hotelMinPriceRepository.findHotelsWithAvailableInventory(
            hotelSearchRequest.getCity(),
            hotelSearchRequest.getStartDate(),
            hotelSearchRequest.getEndDate(),
            hotelSearchRequest.getRoomsCount(),
            dateCount,
            pageable
        );

        return hotelPage;
    }

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found with Id:"+roomId));

        User user = getCurrentUser();

        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("You are not owner of room with id"+roomId);

        return inventoryRepository.findByRoomOrderByDate(room).stream()
            .map((element) -> modelMapper.map(element, InventoryDto.class))
            .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found with Id:"+roomId));

        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("You are not owner of room with id"+roomId);

        inventoryRepository.getInventoryAndLockBeforeUpdate(roomId, updateInventoryRequestDto.getStarDate(), updateInventoryRequestDto.getEndDate());

        inventoryRepository.updateInventory(roomId, updateInventoryRequestDto.getStarDate(), updateInventoryRequestDto.getEndDate(), updateInventoryRequestDto.getClosd(), updateInventoryRequestDto.getSurgeFactor());


    }

}
