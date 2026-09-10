package com.example.airBnbApp.entity;

import lombok.Setter;
import lombok.Getter;
import jakarta.persistence.Embeddable;

@Getter
@Setter
@Embeddable 
public class HotelContactInfo {
    private String address;
    private String phoneNumber;
    private String email;
    private String location;
}
