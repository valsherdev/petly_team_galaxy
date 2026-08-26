package com.makersacademy.petly.repository;

import com.makersacademy.petly.model.Pet;
import org.springframework.data.repository.CrudRepository;

public interface PetRepository extends CrudRepository<Pet, Long> {
}
