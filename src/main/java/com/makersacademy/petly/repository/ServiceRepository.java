package com.makersacademy.petly.repository;

import com.makersacademy.petly.model.Service;
import com.makersacademy.petly.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ServiceRepository extends CrudRepository<Service, Long> {
    List<Service> findByProvider(User provider);
    List<Service> findByProviderId(Long providerId);
    List<Service> findByType(String type);
}
