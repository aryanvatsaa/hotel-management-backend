package com.example.airBnbApp.strategy;

import java.math.BigDecimal;


import com.example.airBnbApp.entity.Inventory;


public class BasePricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        return inventory.getRoom().getBasePrice();
    }

}
