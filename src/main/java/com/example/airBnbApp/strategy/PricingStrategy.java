package com.example.airBnbApp.strategy;

import com.example.airBnbApp.entity.Inventory;
import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
