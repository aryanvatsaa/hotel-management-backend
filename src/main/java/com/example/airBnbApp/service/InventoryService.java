package com.example.airBnbApp.service;
import org.springframework.data.domain.Page;

import com.example.airBnbApp.dto.HotelDto;
import com.example.airBnbApp.dto.HotelSearchRequest;
import com.example.airBnbApp.entity.Room;


public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    

}
