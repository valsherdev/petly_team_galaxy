package com.makersacademy.petly.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "BOOKINGS")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status = "PENDING";

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private User provider;

    public Booking() {}

    public Booking(Pet pet, Service service, LocalDateTime startTime, LocalDateTime endTime, User owner, User provider) {
        this.pet = pet;
        this.service = service;
        this.startTime = startTime;
        this.endTime = endTime;
        this.owner = owner;
        this.provider = provider;
    }
}
