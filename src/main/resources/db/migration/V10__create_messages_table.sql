CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id),
    recipient_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_sender_recipient ON messages (sender_id, recipient_id);
CREATE INDEX idx_messages_recipient_sender ON messages (recipient_id, sender_id);