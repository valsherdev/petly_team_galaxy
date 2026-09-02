package com.makersacademy.petly.feature;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class ServiceDistanceFeatureTest {

    WebDriver driver;
    Faker faker;
    WebDriverWait wait;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        faker = new Faker();
        jdbcTemplate.update("TRUNCATE TABLE bookings, services, pets, users RESTART IDENTITY CASCADE");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        jdbcTemplate.update("DELETE FROM bookings");
        jdbcTemplate.update("DELETE FROM services");
        jdbcTemplate.update("DELETE FROM pets");
        jdbcTemplate.update("DELETE FROM users");
    }

    private Long signUpAs(String email, String role, String name) {
        driver.get("http://localhost:8081/");
        driver.findElement(By.linkText("Sign up")).click();
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys("P@55qw0rd");
        driver.findElement(By.name("action")).click();

        wait.until(ExpectedConditions.urlContains("/users/select-role"));

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, email);

        jdbcTemplate.update("UPDATE users SET role = ?, name = ? WHERE id = ?", role, name, userId);

        String dashboardUrl = "SERVICE_PROVIDER".equals(role)
                ? "http://localhost:8081/dashboard/provider"
                : "http://localhost:8081/dashboard/owner";
        driver.get(dashboardUrl);

        return userId;
    }

    private Long insertPet(String name, String type, Long ownerId) {
        jdbcTemplate.update(
                "INSERT INTO pets (name, type, breed, age, description, photo, owner_id) VALUES (?, ?, 'Breed', 3, 'A pet description', 'photo.jpg', ?)",
                name, type, ownerId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM pets WHERE name = ? AND owner_id = ?", Long.class, name, ownerId);
    }

    private Long insertServiceProvider(String username, String name, String postcode, Double lat, Double lng) {
        jdbcTemplate.update(
                "INSERT INTO users (username, enabled, role, name, location, latitude, longitude) VALUES (?, true, 'SERVICE_PROVIDER', ?, ?, ?, ?)",
                username, name, postcode, lat, lng);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, username);
    }

    private Long insertService(String name, Double price, String providerLocation, Long providerId, Double lat, Double lng) {
        jdbcTemplate.update(
                "INSERT INTO services (name, type, price, price_unit, location, description, provider_id, duration, latitude, longitude) " +
                        "VALUES (?, 'PET_CARE', ?, 'PER_HOUR', ?, 'A test service description', ?, 60, ?, ?)",
                name, price, providerLocation, providerId, lat, lng);

        return jdbcTemplate.queryForObject(
                "SELECT id FROM services WHERE name = ? AND provider_id = ?", Long.class, name, providerId);
    }

    private Long insertBooking(Long petId, Long serviceId, Long ownerId, Long providerId, String status) {
        jdbcTemplate.update(
                "INSERT INTO bookings (pet_id, service_id, start_time, end_time, status, owner_id, provider_id) " +
                        "VALUES (?, ?, '2026-09-01 13:35:00', '2026-09-01 15:35:00', ?, ?, ?)",
                petId, serviceId, status, ownerId, providerId);

        return jdbcTemplate.queryForObject(
                "SELECT id FROM bookings WHERE pet_id = ? AND service_id = ? AND owner_id = ?",
                Long.class, petId, serviceId, ownerId);
    }


}
