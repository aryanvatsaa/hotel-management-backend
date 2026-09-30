package com.example.airBnbApp.service;
import com.example.airBnbApp.entity.Room;


public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    

}
