package com.makersacademy.petly.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class RatingTest {

    private User owner = new User("owner@example.com");
    private User provider = new User("provider@example.com");
    private Pet pet = new Pet();
    private Service service = new Service(provider, "Dog Grooming", "GROOMING",
            new BigDecimal("30.00"), "FIXED", "SW1A 1AA", "A grooming service");
    private Booking booking = new Booking(pet, service,
            LocalDateTime.of(2026, 1, 1, 9, 0),
            LocalDateTime.of(2026, 1, 1, 10, 0),
            owner, provider);
    private Rating rating = new Rating(booking, service, owner, 5);

    @Test
    public void ratingHasBookingServiceOwnerAndStars() {
        assertThat(rating.getBooking(), is(booking));
        assertThat(rating.getService(), is(service));
        assertThat(rating.getOwner(), is(owner));
        assertThat(rating.getStars(), is(5));
    }

    @Test
    public void ratingHasCreatedAtSetOnConstruction() {
        assertThat(rating.getCreatedAt(), is(notNullValue()));
    }

    @Test
    public void ratingIdIsNullBeforePersisting() {
        assertThat(rating.getId(), is(nullValue()));
    }

    @Test
    public void noArgsRatingHasNoFieldsSet() {
        Rating emptyRating = new Rating();

        assertThat(emptyRating.getBooking(), is(nullValue()));
        assertThat(emptyRating.getService(), is(nullValue()));
        assertThat(emptyRating.getOwner(), is(nullValue()));
        assertThat(emptyRating.getStars(), is(nullValue()));
    }
}