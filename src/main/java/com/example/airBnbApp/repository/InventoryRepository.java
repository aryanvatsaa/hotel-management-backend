package com.example.airBnbApp.repository;

import com.example.airBnbApp.entity.Inventory;
import com.example.airBnbApp.entity.Room;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long>{


    void deleteByDateAfterAndRoom(LocalDate date, Room room);
}
