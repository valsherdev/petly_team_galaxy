package com.makersacademy.petly.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingTest {

    private User owner = new User("owner@example.com");
    private User provider = new User("provider@example.com");
    private Pet pet = new Pet();
    private Service service = new Service(provider, "Dog Boarding", "PET_CARE",
            new BigDecimal("25.00"), "PER_DAY", "London", "Overnight boarding");
    private LocalDateTime startTime = LocalDateTime.of(2026, 9, 1, 9, 0);
    private LocalDateTime endTime = LocalDateTime.of(2026, 9, 2, 9, 0);
    private Booking booking = new Booking(pet, service, startTime, endTime, owner, provider);

    @Test
    public void bookingHasPet() {
        assertThat(booking.getPet(), is(pet));
    }

    @Test
    public void bookingHasService() {
        assertThat(booking.getService(), is(service));
    }

    @Test
    public void bookingHasStartAndEndTime() {
        assertThat(booking.getStartTime(), is(startTime));
        assertThat(booking.getEndTime(), is(endTime));
    }

    @Test
    public void bookingHasOwnerAndProvider() {
        assertThat(booking.getOwner(), is(owner));
        assertThat(booking.getProvider(), is(provider));
    }

    @Test
    public void bookingIdIsNullBeforePersisting() {
        assertThat(booking.getId(), is(nullValue()));
    }

    @Test
    public void bookingStatusDefaultsToPending() {
        assertThat(booking.getStatus(), is("PENDING"));
    }

    @Test
    public void bookingStatusCanBeChanged() {
        booking.setStatus("CONFIRMED");

        assertThat(booking.getStatus(), is("CONFIRMED"));
    }

    @Test
    public void noArgsBookingHasNoFieldsSet() {
        Booking emptyBooking = new Booking();

        assertThat(emptyBooking.getPet(), is(nullValue()));
        assertThat(emptyBooking.getService(), is(nullValue()));
        assertThat(emptyBooking.getStatus(), is("PENDING"));
    }

}
