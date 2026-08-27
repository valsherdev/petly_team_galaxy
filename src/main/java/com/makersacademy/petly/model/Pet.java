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

    public Pet() {
    }

    public Pet(Long id, String name, String type, String breed, Integer age, String description, String photo, User owner) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.age = age;
        this.description = description;
        this.photo = photo;
        this.owner = owner;
    }
}


