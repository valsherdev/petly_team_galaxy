package com.makersacademy.petly.feature;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class BookingFeatureTest {
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

    private Long insertProvider(String username, String name) {
        jdbcTemplate.update(
                "INSERT INTO users (username, enabled, role, name) VALUES (?, true, 'SERVICE_PROVIDER', ?)",
                username, name);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, username);
    }

    private Long insertService(Long providerId, String name, String type, Integer durationMinutes) {
        Long durationSeconds = (durationMinutes == null) ? null : durationMinutes * 60L;
        jdbcTemplate.update(
                "INSERT INTO services (name, type, price, price_unit, description, provider_id, duration) " +
                        "VALUES (?, ?, 25.00, 'FIXED', 'A test service', ?, ?)",
                name, type, providerId, durationSeconds);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM services WHERE name = ? AND provider_id = ?",
                Long.class, name, providerId);
    }

    private Long insertPet(Long ownerId, String name) {
        jdbcTemplate.update(
                "INSERT INTO pets (name, type, breed, age, description, owner_id) " +
                        "VALUES (?, 'DOG', 'Test Breed', 3, 'A test pet', ?)",
                name, ownerId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM pets WHERE name = ? AND owner_id = ?", Long.class, name, ownerId);
    }

    private void insertBooking(Long petId, Long serviceId, Long ownerId, Long providerId,
                               String startTime, String endTime, String status) {
        jdbcTemplate.update(
                "INSERT INTO bookings (pet_id, service_id, start_time, end_time, status, owner_id, provider_id) " +
                        "VALUES (?, ?, ?::timestamp, ?::timestamp, ?, ?, ?)",
                petId, serviceId, startTime, endTime, status, ownerId, providerId);
    }

    private int bookingCountFor(Long serviceId, Long ownerId, String status) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bookings WHERE service_id = ? AND owner_id = ? AND status = ?",
                Integer.class, serviceId, ownerId, status);

    }

    private void setDateTimeLocal(WebElement input, String isoValue) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];" +
                        "arguments[0].dispatchEvent(new Event('change'));",
                input, isoValue);
    }

    @Test
    public void ownerCanRequestBookingForDurationBasedService() {

        Long providerId = insertProvider(faker.name().username() + "@email.com", "Test Provider");
        Long serviceId = insertService(providerId, "Grooming " + faker.number().digits(6), "GROOMING", 60);

        String ownerEmail = faker.name().username() + "@email.com";
        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Test Owner");
        Long petId = insertPet(ownerId, "Rex");

        driver.get("http://localhost:8081/services/" + serviceId + "/book");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("startTime")));

        new Select(driver.findElement(By.id("petId"))).selectByValue(petId.toString());
        setDateTimeLocal(driver.findElement(By.id("startTime")), "2027-01-15T10:00");
        driver.findElement(By.cssSelector("form[action='/bookings/create'] button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/dashboard/owner"));

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(1, bookingCountFor(serviceId, ownerId, "PENDING")));
    }

}
