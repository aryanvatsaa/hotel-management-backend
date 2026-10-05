package com.example.airBnbApp.strategy;

import java.math.BigDecimal;

import com.example.airBnbApp.entity.Inventory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
public class HolidayPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;
    
    
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        boolean isTodayHoliday = true;      //call an API or check with local Data

        if(isTodayHoliday) {
            price = price.multiply(BigDecimal.valueOf(1.25));
        }
        return price;
    }

}
