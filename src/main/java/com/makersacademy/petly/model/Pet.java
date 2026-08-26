package com.makersacademy.petly.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "PETS")
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private String breed;
    private Integer age;
    private String description;
    private String photo;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    public Pet() {}
}

