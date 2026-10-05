package com.example.airBnbApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.airBnbApp.entity.Hotel;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class HotelPriceDto {
    private Hotel hotel;
    private Double price;

}
