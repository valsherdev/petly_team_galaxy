package com.makersacademy.petly.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PetTest {
    private final User owner = new User(1L, "test@test.com", true, "Owner", "John");
    private final Pet pet = new Pet(1L, "Toby", "Dog", "Golden Retriever", 6, "The goodest boy", "url", owner);

    @Test
    public void userCreatedIsOwner() {
        assertThat(pet.getOwner().getUsername(), is("test@test.com"));
    }

    @Test
    public void petCreationReturnsInformation() {
        assertThat(pet.getId(), is(1L));
        assertThat(pet.getName(), containsString("Toby"));
        assertThat(pet.getType(), containsString("Dog"));
        assertThat(pet.getBreed(), containsString("Golden Retriever"));
        assertThat(pet.getAge(), is(6));
        assertThat(pet.getDescription(), containsString("The goodest boy"));
        assertThat(pet.getPhoto(), containsString("url"));
    }
}
