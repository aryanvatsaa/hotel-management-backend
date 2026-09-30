package com.example.airBnbApp.repository;

import com.example.airBnbApp.entity.Inventory;
import com.example.airBnbApp.entity.Room;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long>{


    void deleteByRoom(Room room);
}
