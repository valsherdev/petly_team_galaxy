package com.makersacademy.petly.feature;


import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class RatingFeatureTest {

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

        jdbcTemplate.update("DELETE FROM ratings");
        jdbcTemplate.update("DELETE FROM messages");
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

    private Long insertUser(String username, String role, String name) {
        jdbcTemplate.update(
                "INSERT INTO users (username, enabled, role, name) VALUES (?, true, ?, ?)",
                username, role, name);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, username);
    }

    private Long insertService(Long providerId, String name, String type, Integer durationMinutes) {
        Long durationSeconds = (durationMinutes == null) ? null : durationMinutes * 60L;
        jdbcTemplate.update(
                "INSERT INTO services (name, type, price, price_unit, description, provider_id, duration, location) " +
                        "VALUES (?, ?, 25.00, 'FIXED', 'A test service', ?, ?, 'SW1A 1AA')",
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

    private Long insertBooking(Long petId, Long serviceId, Long ownerId, Long providerId,
                               String startTime, String endTime, String status) {
        jdbcTemplate.update(
                "INSERT INTO bookings (pet_id, service_id, start_time, end_time, status, owner_id, provider_id) " +
                        "VALUES (?, ?, ?::timestamp, ?::timestamp, ?, ?, ?)",
                petId, serviceId, startTime, endTime, status, ownerId, providerId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM bookings WHERE service_id = ? AND owner_id = ? AND start_time = ?::timestamp",
                Long.class, serviceId, ownerId, startTime);
    }

    private void insertRating(Long bookingId, Long serviceId, Long ownerId, int stars) {
        jdbcTemplate.update(
                "INSERT INTO ratings (booking_id, service_id, owner_id, stars) VALUES (?, ?, ?, ?)",
                bookingId, serviceId, ownerId, stars);
    }

    private int ratingCountFor(Long bookingId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ratings WHERE booking_id = ?", Integer.class, bookingId);
    }


    @Test
    public void ownerCanRatePastBooking() {
        Long providerId = insertUser(faker.name().username() + "@email.com", "SERVICE_PROVIDER", "Test Provider");
        Long serviceId = insertService(providerId, "Grooming " + faker.number().digits(6), "GROOMING", 30);

        String ownerEmail = faker.name().username() + "@email.com";
        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Test Owner");
        Long petId = insertPet(ownerId, "Rex");

        Long bookingId = insertBooking(petId, serviceId, ownerId, providerId,
                "2020-01-01 09:00:00", "2020-01-01 09:30:00", "CONFIRMED");

        driver.get("http://localhost:8081/bookings/" + bookingId + "/rate");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("stars")));

        new Select(driver.findElement(By.name("stars"))).selectByValue("5");
        driver.findElement(
                By.cssSelector("form[action='/bookings/" + bookingId + "/rate'] button[type='submit']")).click();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Integer stars = jdbcTemplate.queryForObject(
                    "SELECT stars FROM ratings WHERE booking_id = ?", Integer.class, bookingId);
            assertEquals(5, stars);
        });
    }

    @Test
    public void ownerCannotRateSameBookingTwice() {
        Long providerId = insertUser(faker.name().username() + "@email.com", "SERVICE_PROVIDER", "Test Provider");
        Long serviceId = insertService(providerId, "Grooming " + faker.number().digits(6), "GROOMING", 30);

        String ownerEmail = faker.name().username() + "@email.com";
        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Test Owner");
        Long petId = insertPet(ownerId, "Milo");

        Long bookingId = insertBooking(petId, serviceId, ownerId, providerId,
                "2020-02-01 09:00:00", "2020-02-01 09:30:00", "CONFIRMED");
        insertRating(bookingId, serviceId, ownerId, 4);

        driver.get("http://localhost:8081/bookings/" + bookingId + "/rate");

        wait.until(ExpectedConditions.urlContains("/dashboard/owner/bookings"));
        assertEquals(1, ratingCountFor(bookingId));
    }


    @Test
    public void ownerCannotRateBookingThatIsNotPast() {
        Long providerId = insertUser(faker.name().username() + "@email.com", "SERVICE_PROVIDER", "Test Provider");
        Long serviceId = insertService(providerId, "Grooming " + faker.number().digits(6), "GROOMING", 30);

        String ownerEmail = faker.name().username() + "@email.com";
        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Test Owner");
        Long petId = insertPet(ownerId, "Luna");


        Long bookingId = insertBooking(petId, serviceId, ownerId, providerId,
                "2027-06-01 09:00:00", "2027-06-01 09:30:00", "CONFIRMED");

        driver.get("http://localhost:8081/bookings/" + bookingId + "/rate");

        wait.until(ExpectedConditions.urlContains("/dashboard/owner/bookings"));
        assertEquals(0, ratingCountFor(bookingId));
    }

    @Test
    public void averageRatingDisplaysOnServicesPage() {
        Long providerId = insertUser(faker.name().username() + "@email.com", "SERVICE_PROVIDER", "Rated Provider");
        String serviceName = "Rated Service " + faker.number().digits(6);
        Long serviceId = insertService(providerId, serviceName, "GROOMING", 30);

        Long otherOwnerId = insertUser(faker.name().username() + "@email.com", "PET_OWNER", "Other Owner");
        Long otherPetId = insertPet(otherOwnerId, "Buddy");
        Long ratedBookingId = insertBooking(otherPetId, serviceId, otherOwnerId, providerId,
                "2020-03-01 09:00:00", "2020-03-01 09:30:00", "CONFIRMED");
        insertRating(ratedBookingId, serviceId, otherOwnerId, 4);

        String viewerEmail = faker.name().username() + "@email.com";
        signUpAs(viewerEmail, "PET_OWNER", "Test Viewer");

        driver.get("http://localhost:8081/services");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("4.0"));
    }


    @Test
    public void userCannotRateSomeoneElsesBooking() {
        Long providerId = insertUser(faker.name().username() + "@email.com", "SERVICE_PROVIDER", "Test Provider");
        Long serviceId = insertService(providerId, "Grooming " + faker.number().digits(6), "GROOMING", 30);

        Long ownerAId = insertUser(faker.name().username() + "@email.com", "PET_OWNER", "Owner A");
        Long ownerAPetId = insertPet(ownerAId, "Bella");
        Long ownerABookingId = insertBooking(ownerAPetId, serviceId, ownerAId, providerId,
                "2020-04-01 09:00:00", "2020-04-01 09:30:00", "CONFIRMED");

        String ownerBEmail = faker.name().username() + "@email.com";
        signUpAs(ownerBEmail, "PET_OWNER", "Owner B");

        driver.get("http://localhost:8081/bookings/" + ownerABookingId + "/rate");

        wait.until(ExpectedConditions.urlContains("/dashboard/owner/bookings"));
        assertEquals(0, ratingCountFor(ownerABookingId));
    }

    @Test
    public void averageRatingDisplaysCorrectly() {
        Long providerId = insertUser(faker.name().username() + "@email.com", "SERVICE_PROVIDER", "Rated Provider");
        String serviceName = "Multi Rated Service " + faker.number().digits(6);
        Long serviceId = insertService(providerId, serviceName, "GROOMING", 30);

        Long owner1Id = insertUser(faker.name().username() + "@email.com", "PET_OWNER", "Owner One");
        Long pet1Id = insertPet(owner1Id, "Rex");
        Long booking1Id = insertBooking(pet1Id, serviceId, owner1Id, providerId,
                "2020-05-01 09:00:00", "2020-05-01 09:30:00", "CONFIRMED");
        insertRating(booking1Id, serviceId, owner1Id, 5);

        Long owner2Id = insertUser(faker.name().username() + "@email.com", "PET_OWNER", "Owner Two");
        Long pet2Id = insertPet(owner2Id, "Daisy");
        Long booking2Id = insertBooking(pet2Id, serviceId, owner2Id, providerId,
                "2020-05-02 09:00:00", "2020-05-02 09:30:00", "CONFIRMED");
        insertRating(booking2Id, serviceId, owner2Id, 3);


        String viewerEmail = faker.name().username() + "@email.com";
        signUpAs(viewerEmail, "PET_OWNER", "Test Viewer");

        driver.get("http://localhost:8081/services");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("4.0"));
        assertTrue(pageText.contains("(2)"));
    }


    @Test
    public void averageRatingDisplaysCorrectlyWithDecimal() {
        Long providerId = insertUser(faker.name().username() + "@email.com", "SERVICE_PROVIDER", "Rated Provider");
        String serviceName = "Decimal Rated Service " + faker.number().digits(6);
        Long serviceId = insertService(providerId, serviceName, "GROOMING", 30);

        Long owner1Id = insertUser(faker.name().username() + "@email.com", "PET_OWNER", "Owner One");
        Long pet1Id = insertPet(owner1Id, "Max");
        Long booking1Id = insertBooking(pet1Id, serviceId, owner1Id, providerId,
                "2020-06-01 09:00:00", "2020-06-01 09:30:00", "CONFIRMED");
        insertRating(booking1Id, serviceId, owner1Id, 5);

        Long owner2Id = insertUser(faker.name().username() + "@email.com", "PET_OWNER", "Owner Two");
        Long pet2Id = insertPet(owner2Id, "Ruby");
        Long booking2Id = insertBooking(pet2Id, serviceId, owner2Id, providerId,
                "2020-06-02 09:00:00", "2020-06-02 09:30:00", "CONFIRMED");
        insertRating(booking2Id, serviceId, owner2Id, 4);

        String viewerEmail = faker.name().username() + "@email.com";
        signUpAs(viewerEmail, "PET_OWNER", "Test Viewer");

        driver.get("http://localhost:8081/services");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("4.5"));
    }

}


