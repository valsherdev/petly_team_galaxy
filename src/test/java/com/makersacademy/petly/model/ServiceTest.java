package com.makersacademy.petly.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class ServiceTest {

    private User provider = new User("provider@example.com");
    private Service service = new Service(provider, "Dog Grooming", "GROOMING",
            new BigDecimal("40.00"), "FIXED", "Central London", "Full wash and trim");

    @Test
    public void serviceHasProvider() {
        assertThat(service.getProvider(), is(provider));
    }

    @Test
    public void serviceHasName() {
        assertThat(service.getName(), containsString("Dog Grooming"));
    }

    @Test
    public void serviceHasType() {
        assertThat(service.getType(), is("GROOMING"));
    }

    @Test
    public void serviceHasPriceAndPriceUnit() {
        assertThat(service.getPrice(), is(new BigDecimal("40.00")));
        assertThat(service.getPriceUnit(), is("FIXED"));
    }

    @Test
    public void serviceHasLocationAndDescription() {
        assertThat(service.getLocation(), containsString("London"));
        assertThat(service.getDescription(), containsString("wash and trim"));
    }

    @Test
    public void constructorDoesNotSetDuration() {
        assertThat(service.getDuration(), is(nullValue()));
        assertThat(service.getDurationMinutes(), is(nullValue()));
    }

    @Test
    public void setDurationMinutesConvertsToDurationCorrectly() {
        service.setDurationMinutes(90);

        assertThat(service.getDuration(), is(Duration.ofMinutes(90)));
    }

    @Test
    public void getDurationMinutesConvertsBackFromDurationCorrectly() {
        service.setDuration(Duration.ofMinutes(45));

        assertThat(service.getDurationMinutes(), is(45));
    }

    @Test
    public void durationMinutesRoundTripsCorrectly() {
        service.setDurationMinutes(120);

        assertThat(service.getDurationMinutes(), is(120));
        assertThat(service.getDuration(), is(Duration.ofHours(2)));
    }

}
