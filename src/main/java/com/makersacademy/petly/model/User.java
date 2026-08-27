package com.makersacademy.petly.model;

import jakarta.persistence.*;
import lombok.Data;


import static java.lang.Boolean.TRUE;

@Data
@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private boolean enabled;
    private String role;
    private String name;

    public User() {
        this.enabled = TRUE;
    }

    public User(String username) {
        this.username = username;
        this.enabled = TRUE;
    }

    public User(String username, boolean enabled) {
        this.username = username;
        this.enabled = enabled;
    }

    public User(long id, String username, boolean enabled, String role, String name) {
        this.id = id;
        this.username = username;
        this.enabled = enabled;
        this.role = role;
        this.name = name;
    }
}
