package com.example.airBnbApp.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor 
@Data
public class HotelInfoDto {
    private HotelDto hotel;
    private List<RoomDto> rooms;
}
