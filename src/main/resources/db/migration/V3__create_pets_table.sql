CREATE TABLE pets (
    id bigserial PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    breed VARCHAR(50) NOT NULL,
    age INTEGER NOT NULL,
    description TEXT NOT NULL,
    photo TEXT,
    owner_id BIGINT NOT NULL REFERENCES users(id) on DELETE CASCADE
);