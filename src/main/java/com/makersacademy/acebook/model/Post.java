package com.makersacademy.acebook.model;

import jakarta.persistence.*;

import lombok.Data;

@Data
@Entity
@Table(name = "POSTS")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;

    public Post() {}

    public Post(String content) {
        this.content = content;
    }

}
