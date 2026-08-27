package com.makersacademy.petly.model;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Duration;

@Data
@Entity
@Table(name = "SERVICES")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private BigDecimal price;
    private String priceUnit;
    private String location;
    private Double latitude;
    private Double longitude;
    private String description;
    private Duration duration;
    private String adminDistrict;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private User provider;

    public Service() {};

    public Service(User provider, String name, String type, BigDecimal price, String priceUnit, String location, String description) {
        this.provider = provider;
        this.name = name;
        this.type = type;
        this.price = price;
        this.priceUnit = priceUnit;
        this.location = location;
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return duration == null ? null : (int) duration.toMinutes();
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.duration = (durationMinutes == null) ? null : Duration.ofMinutes(durationMinutes);
    }

}
