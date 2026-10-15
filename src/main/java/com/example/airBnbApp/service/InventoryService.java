package com.example.airBnbApp.service;
import java.util.List;
import org.springframework.data.domain.Page;
import com.example.airBnbApp.dto.HotelPriceDto;

import com.example.airBnbApp.dto.HotelSearchRequest;
import com.example.airBnbApp.dto.InventoryDto;
import com.example.airBnbApp.dto.UpdateInventoryRequestDto;
import com.example.airBnbApp.entity.Room;


public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);

    

}
