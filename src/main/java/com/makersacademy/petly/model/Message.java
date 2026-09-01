package com.makersacademy.petly.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "MESSAGES")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    private String content;

    private boolean read = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Message() {}

    public Message(User sender, User recipient, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }
}