package com.example.airBnbApp.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.airBnbApp.entity.Hotel;
import com.example.airBnbApp.entity.Inventory;
import com.example.airBnbApp.entity.Room;

public interface InventoryRepository extends JpaRepository<Inventory, Long>{

    
    void deleteByRoom(Room room);

    @Query("""
        SELECT DISTINCT i.hotel
        FROM Inventory i
        WHERE i.city = :city
            AND i.date BETWEEN :startDate AND :endDate
            AND i.closed = false
            AND (i.totalCount - i.bookedCount) >= :roomsCount
        GROUP BY i.hotel, i.room
        HAVING COUNT(i.date) = :dateCount
        """) 

    Page<Hotel> findHotelsWithAvailableInventory(
        @Param("city") String city,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("roomsCount") Integer roomsCount,
        @Param("dateCount") Long dateCount,
        Pageable pageable
        );
}
