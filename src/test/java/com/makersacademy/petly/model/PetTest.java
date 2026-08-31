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

    @Test
    public void testAddingAPet() {
        Pet emptyPet = new Pet();
        emptyPet.setId(1L);
        emptyPet.setName("Jane");
        emptyPet.setType("Dog");
        emptyPet.setBreed("Cockapoo");
        emptyPet.setAge(6);
        emptyPet.setDescription("I am a dog");
        emptyPet.setPhoto("url");
        emptyPet.setOwner(owner);

        assertThat(emptyPet.getId(), is(1L));
        assertThat(emptyPet.getName(), is("Jane"));
        assertThat(emptyPet.getType(), is("Dog"));
        assertThat(emptyPet.getBreed(), is("Cockapoo"));
        assertThat(emptyPet.getAge(), is(6));
        assertThat(emptyPet.getDescription(), is("I am a dog"));
        assertThat(emptyPet.getPhoto(), is("url"));
        assertThat(emptyPet.getOwner(), is(owner));
    }

    @Test
    public void testLombokToStringEqualsAndHashCode() {
        Pet p1 = new Pet(1L, "Toby", "Dog", "Golden Retriever", 6, "The goodest boy", "url", owner);
        Pet p2 = new Pet(1L, "Toby", "Dog", "Golden Retriever", 6, "The goodest boy", "url", owner);

        // toString() - tests to see that the object as a string looks how the actual object should look
        assertThat(p1.toString(), containsString("Pet"));

        // hashcode - like a digital footprint
        assertThat(p1.hashCode(), is(p2.hashCode()));

        // equals - testing whether p1 is the same and matches whatever is in the bracket
        assertThat(p1.equals(p2), is(true));
        assertThat(p1.equals(null), is(false));
        assertThat(p1.equals(new Object()), is(false));
    }
}
