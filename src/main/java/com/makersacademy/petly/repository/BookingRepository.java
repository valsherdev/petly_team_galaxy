package com.makersacademy.petly.repository;

import com.makersacademy.petly.model.Booking;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BookingRepository extends CrudRepository<Booking, Long> {
    List<Booking> findByServiceId(Long serviceId);
    List<Booking> findByOwnerId(Long ownerId);
    List<Booking> findByProviderId(Long providerId);
    List<Booking> findByProviderIdAndStatus(Long providerId, String status);
    List<Booking> findByOwnerIdAndStatus(Long ownerId, String status);
}
