package com.makersacademy.petly.repository;

import com.makersacademy.petly.model.Message;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Long> {

    @Query("SELECT m FROM Message m " +
    "WHERE (m.sender.id = :userId1 AND m.recipient.id = :userId2)" +
    "OR (m.sender.id = :userId2 AND m.recipient.id = :userId1)" +
    "ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT m  FROM Message m " +
    "WHERE (m.sender.id = :userId OR m.recipient.id = :userId)" +
    "ORDER BY m.createdAt DESC")
    List<Message> findAllInvolvingUser(@Param("userId") Long userId);

    long countByRecipientIdAndSenderIdAndReadFalse(Long recipientId, Long senderId);

    long countByRecipientIdAndReadFalse(Long recipientId);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.read = true " +
            "WHERE m.recipient.id = :recipientId AND m.sender.id = :senderId AND m.read = false")
    void markThreadAsRead(@Param("recipientId") Long recipientId, @Param("senderId") Long senderId);

}
