package com.makersacademy.petly.repository;

import com.makersacademy.petly.model.Rating;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends CrudRepository<Rating, Long> {

    long countByServiceId(Long serviceId);
    List<Rating> findByOwnerId(Long ownerId);
    boolean existsByBookingId(Long bookingId);

    @Query("SELECT AVG(rating.stars) FROM Rating rating WHERE rating.service.id = :serviceId")
    Double findAverageStarsByServiceId(@Param("serviceId") Long serviceId);

}
