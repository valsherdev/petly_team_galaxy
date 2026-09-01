package com.makersacademy.petly.model;

import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class MessageTest {

    private User sender = new User("sender@example.com");
    private User recipient = new User("recipient@example.com");
    private Message message = new Message(sender, recipient, "Hi there");

    @Test
    public void messageHasSenderAndRecipient() {
        assertThat(message.getSender(), is(sender));
        assertThat(message.getRecipient(), is(recipient));
    }

    @Test
    public void messageHasContent() {
        assertThat(message.getContent(), containsString("Hi there"));
    }

    @Test
    public void messageDefaultsToUnread() {
        assertThat(message.isRead(), is(false));
    }

    @Test
    public void messageCanBeMarkedRead() {
        message.setRead(true);

        assertThat(message.isRead(), is(true));
    }

    @Test
    public void messageHasCreatedAtSet() {
        assertThat(message.getCreatedAt(), is(notNullValue()));
    }

    @Test
    public void noArgsMessageDefaultsToUnreadWithNoFieldsSet() {
        Message emptyMessage = new Message();

        assertThat(emptyMessage.getSender(), is(nullValue()));
        assertThat(emptyMessage.getRecipient(), is(nullValue()));
        assertThat(emptyMessage.isRead(), is(false));
    }
}

