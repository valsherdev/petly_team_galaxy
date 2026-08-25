CREATE TABLE services
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    price DECIMAL NOT NULL,
    price_unit VARCHAR(20) NOT NULL,
    location VARCHAR(50),
    description TEXT NOT NULL,
    provider_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    duration INTERVAL
);