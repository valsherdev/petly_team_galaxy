package com.makersacademy.petly.feature;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private Long insertServiceProvider(String username) {
        jdbcTemplate.update(
                "INSERT INTO users (username, enabled, role, name, location) VALUES (?, true, 'SERVICE_PROVIDER', 'Alex', 'SW1A 1AA')",
                username);
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

    @Test
    @DisplayName("Pet owner can see distance in km to provider's service on the services page")
    public void ownerSeesDistanceOnServicesPage() {
        String providerEmail = faker.name().username() + "@email.com";
        String ownerEmail = faker.name().username()+ "@email.com";

        Long providerId = insertServiceProvider(providerEmail);

        insertService("London dog grooming", 40.00, "SW1A 1AA", providerId, 51.50101, -0.141563);

        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Manchester Owner");
        jdbcTemplate.update("UPDATE users SET location = 'M40 8BZ', latitude = 53.500452, longitude = -2.204787 WHERE id = ?", ownerId);

        driver.get("http://localhost:8081/services");
        driver.findElement(By.cssSelector("[data-testid='service_name']"));

        String pageSource = driver.getPageSource();

        assertTrue(pageSource.contains("London dog grooming"));
        assertTrue(pageSource.contains("262.5 km away"));
    }

    @Test
    @DisplayName("Pet owner lives in the same postcode as provider, shows as 0 km")
    public void ownerInSamePostcodeAsService() {
        String providerEmail = faker.name().username() + "@email.com";
        String ownerEmail = faker.name().username()+ "@email.com";

        Long providerId = insertServiceProvider(providerEmail);

        insertService("Pet services", 40.00, "SW1A 1AA", providerId, 51.50101, -0.141563);

        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Manchester Owner");
        jdbcTemplate.update("UPDATE users SET location = 'SW1A 1AA', latitude = 51.50101, longitude = -0.141563 WHERE id = ?", ownerId);

        driver.get("http://localhost:8081/services");
        driver.findElement(By.cssSelector("[data-testid='service_name']"));

        String pageSource = driver.getPageSource();

        assertTrue(pageSource.contains("Pet services"));
        assertTrue(pageSource.contains("0 km away"));
    }

    @Test
    @DisplayName("Services are listed in ascending order of distance from the owner")
    public void ownerSeesServicesOrderedByDistance() {
        String providerEmail = faker.name().username() + "@email.com";
        String ownerEmail = faker.name().username()+ "@email.com";

        Long providerId = insertServiceProvider(providerEmail);

        insertService("London dog grooming", 40.00, "SW1A 1AA", providerId, 51.50101, -0.141563);
        insertService("Manchester pet sitting", 25.00, "M2 4NH", providerId, 53.48072, -2.244412);

        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Manchester Owner");
        jdbcTemplate.update("UPDATE users SET location = 'SW1A 1AA', latitude = 51.50101, longitude = -0.141563 WHERE id = ?", ownerId);

        driver.get("http://localhost:8081/services");
        List<WebElement> serviceTitles = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("[data-testid='service_name']")));

        assertEquals(2, serviceTitles.size());
        assertEquals("London dog grooming", serviceTitles.get(0).getText());
        assertEquals("Manchester pet sitting", serviceTitles.get(1).getText());
    }

    @Test
    @DisplayName("Error message shows if owner inputs invalid postcode")
    public void ownerErrorMessageWhenInvalidPostcodeEnteredIntoProfile() {
        String ownerEmail = faker.name().username()+ "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "Pet Owner");

        driver.get("http://localhost:8081/profile");
        driver.findElement(By.cssSelector("[data-testid='owner-location']")).sendKeys("SW1A 1AAA");
        driver.findElement(By.cssSelector("[data-testid='save-location-btn']")).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='postcode-error']")));
        assertTrue(errorMessage.getText().contains("Please input a valid postcode"));
    }

    @Test
    @DisplayName("Success message shows if owner inputs valid postcode")
    public void ownerSuccessMessageWhenInvalidPostcodeEnteredIntoProfile() {
        String ownerEmail = faker.name().username()+ "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "Pet Owner");

        driver.get("http://localhost:8081/profile");
        driver.findElement(By.cssSelector("[data-testid='owner-location']")).sendKeys("SW1A 1AA");
        driver.findElement(By.cssSelector("[data-testid='save-location-btn']")).click();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='postcode-success']")));
        assertTrue(successMessage.getText().contains("Location updated successfully!"));
    }

    @Test
    @DisplayName("Error message shows if provider inputs invalid postcode for a service")
    public void providerErrorMessageWhenInvalidPostcodeEnteredIntoService() {
        String providerEmail = faker.name().username()+ "@email.com";
        signUpAs(providerEmail, "SERVICE_PROVIDER", "Service Provider");

        driver.findElement(By.cssSelector("[data-testid='service-name']")).sendKeys("London dog grooming");
        driver.findElement(By.cssSelector("[data-testid='service-price']")).sendKeys("40.00");
        driver.findElement(By.cssSelector("[data-testid='service-location']")).sendKeys("SW1A 1AAA");
        driver.findElement(By.cssSelector("[data-testid='service-description']")).sendKeys("A test service description");

        driver.findElement(By.cssSelector("[data-testid='save-service-btn']")).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='postcode-error']")));

        assertTrue(errorMessage.getText().contains("Please input a valid postcode"));
    }

    @Test
    @DisplayName("Error message shows if provider inputs invalid postcode when updating a service")
    public void providerErrorMessageWhenInvalidPostcodeEnteredWhenUpdatingAService() {
        String providerEmail = faker.name().username()+ "@email.com";
        Long providerId = signUpAs(providerEmail, "SERVICE_PROVIDER", "Alex");

        insertService("London dog grooming", 40.00, "SW1A 1AA", providerId, 51.50101, -0.141563);

        driver.get("http://localhost:8081/dashboard/provider");
        driver.findElement(By.cssSelector("[data-testid='edit-service']")).click();

        // had to add duration edit because selenium kept showing duration as 0 even though helper method is seeded as 60?
        WebElement locationInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='duration']")));
        locationInput.clear();
        locationInput.sendKeys("30");

        WebElement durationInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='service-location']")));
        durationInput.clear();
        durationInput.sendKeys("SW1A 1AAA");

        driver.findElement(By.cssSelector("[data-testid='save-changes-btn']")).click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='postcode-error']")));
        assertTrue(errorMessage.getText().contains("Please input a valid postcode"));
    }


}
