package com.example.airBnbApp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data 
public class UpdateInventoryRequestDto {

    private LocalDate starDate;
    private LocalDate endDate;
    private BigDecimal surgeFactor;
    private Boolean closd;
    
}
