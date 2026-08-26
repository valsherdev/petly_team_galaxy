package com.makersacademy.petly.repository;

import com.makersacademy.petly.model.Pet;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PetRepository extends CrudRepository<Pet, Long> {
    List<Pet> findByOwnerId(Long ownerId);
}
