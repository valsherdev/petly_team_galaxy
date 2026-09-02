package com.makersacademy.petly.model;

import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class UserTest {

    private User user = new User("test@example.com");

    @Test
    public void userHasUsername() {
        assertThat(user.getUsername(), is("test@example.com"));
    }

    @Test
    public void userDefaultsToEnabled() {
        assertThat(user.isEnabled(), is(true));
    }

    @Test
    public void userHasNoRoleOrNameByDefault() {
        assertThat(user.getRole(), is(nullValue()));
        assertThat(user.getName(), is(nullValue()));
    }

    @Test
    public void userCanBeAssignedRoleAndName() {
        user.setRole("PET_OWNER");
        user.setName("Alex Smith");

        assertThat(user.getRole(), is("PET_OWNER"));
        assertThat(user.getName(), is("Alex Smith"));
    }

}
