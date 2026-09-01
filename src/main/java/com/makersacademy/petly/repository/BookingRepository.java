package com.makersacademy.petly.repository;

import com.makersacademy.petly.model.Booking;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends CrudRepository<Booking, Long> {
    List<Booking> findByServiceId(Long serviceId);
    List<Booking> findByOwnerId(Long ownerId);
    List<Booking> findByProviderId(Long providerId);
    List<Booking> findByProviderIdAndStatus(Long providerId, String status);
    List<Booking> findByOwnerIdAndStatus(Long ownerId, String status);
    long countByProviderIdAndStatus(Long providerId, String status);
    List<Booking> findByOwnerIdAndStatusAndEndTimeAfter(Long ownerId, String status, LocalDateTime endTime);
    List<Booking> findByOwnerIdAndStatusAndEndTimeBefore(Long ownerId, String status, LocalDateTime endTime);

    List<Booking> findByProviderIdAndStatusAndEndTimeAfter(Long providerId, String status, LocalDateTime endTime);
    List<Booking> findByProviderIdAndStatusAndEndTimeBefore(Long providerId, String status, LocalDateTime endTime);

}
