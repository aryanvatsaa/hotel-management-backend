package com.example.airBnbApp.strategy;

import java.math.BigDecimal;

import com.example.airBnbApp.entity.Inventory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
public class OccupancyPricingStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        double occupancyRate = (double) inventory.getBookedCount() / inventory.getTotalCount();
        if(occupancyRate > 0.0) {
            price = price.multiply(BigDecimal.valueOf(1.2));
        }
        return price;


    }

}
