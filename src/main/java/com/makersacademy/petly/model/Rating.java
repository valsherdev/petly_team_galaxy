package com.makersacademy.petly.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "RATINGS")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id", unique = true)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private Integer stars;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Rating() {}

    public Rating(Booking booking, Service service, User owner, Integer stars) {
        this.booking = booking;
        this.service = service;
        this.owner = owner;
        this.stars = stars;
        this.createdAt = LocalDateTime.now();
    }
}
