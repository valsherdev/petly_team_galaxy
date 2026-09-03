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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class ServiceFilterFeatureTest {

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

    @Test
    public void ownerWithNoPostcodeSeesAllServicesOnServicePageWithPostcodeButNoDistanceFilter() {
        String providerEmail = faker.name().username() + "@email.com";
        String ownerEmail = faker.name().username() + "@email.com";

        Long providerId = insertServiceProvider(providerEmail);

        insertService("London dog grooming", 40.00, "SW1A 1AA", providerId, 51.50101, -0.141563);

        signUpAs(ownerEmail, "PET_OWNER", "Test Owner");

        driver.get("http://localhost:8081/services");

        WebElement serviceCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='service-list-card']")));
        String pageText = driver.findElement(By.cssSelector("[data-testid='service-list-card']")).getText();
        assertTrue(pageText.contains("SW1A 1AA"));
    }

    @Test
    public void ownerWithPostcodeSeesAllServicesOnServicePageWithPostcodeDistanceAndDistanceFilter() {
        String providerEmail = faker.name().username() + "@email.com";
        String ownerEmail = faker.name().username() + "@email.com";

        Long providerId = insertServiceProvider(providerEmail);

        insertService("London dog grooming", 40.00, "SW1A 1AA", providerId, 51.50101, -0.141563);

        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Manchester Owner");
        jdbcTemplate.update("UPDATE users SET location = 'M40 8BZ', latitude = 53.500452, longitude = -2.204787 WHERE id = ?", ownerId);

        driver.get("http://localhost:8081/services");

        WebElement serviceCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='service-list-card']")));
        WebElement distanceFilter = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='distance-filter']")));
        String pageText = driver.findElement(By.cssSelector("[data-testid='service-list-card']")).getText();
        assertTrue(pageText.contains("262.5 km away"));

    }

    @Test
    public void ownerClicksPetCareFilterOnServicePageAndFiltersBy10KmAwayReturnsNoServices() {
        String providerEmail = faker.name().username() + "@email.com";
        String ownerEmail = faker.name().username() + "@email.com";

        Long providerId = insertServiceProvider(providerEmail);

        insertService("London dog grooming", 40.00, "SW1A 1AA", providerId, 51.50101, -0.141563);

        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Manchester Owner");
        jdbcTemplate.update("UPDATE users SET location = 'M40 8BZ', latitude = 53.500452, longitude = -2.204787 WHERE id = ?", ownerId);

        driver.get("http://localhost:8081/services");

        WebElement serviceCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='service-list-card']")));

        String pageText = driver.findElement(By.cssSelector("body")).toString();
        driver.findElement(By.cssSelector("[data-testid='pet-care-filter']")).click();
        driver.findElement(By.cssSelector("select[data-testid='distance-filter'] > option[value='10'")).click();
        assertThat(pageText.contains("No services found"));

    }

    @Test
    public void ownerClicksPetCareFilterOnServicePageAndFiltersBy10KmAwayReturnsService() {
        String providerEmail = faker.name().username() + "@email.com";
        String ownerEmail = faker.name().username() + "@email.com";

        Long providerId = insertServiceProvider(providerEmail);

        insertService("Nottingham Dog Grooming", 40.00, "NG10 5LQ", providerId, 52.918429, -1.29372);

        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Test Owner");
        jdbcTemplate.update("UPDATE users SET location = 'DE72 3DB', latitude = 52.895953, longitude = -1.327382 WHERE id = ?", ownerId);

        driver.get("http://localhost:8081/services");

        WebElement serviceCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='service-list-card']")));

        String pageText = driver.findElement(By.cssSelector("body")).toString();
        driver.findElement(By.cssSelector("[data-testid='pet-care-filter']")).click();
        driver.findElement(By.cssSelector("select[data-testid='distance-filter'] > option[value='10'")).click();
        assertThat(pageText.contains("Nottingham Dog Grooming"));
    }

    @Test
    public void ownerAndServiceExactly10KmAwayStillReturnsService() {
        String providerEmail = faker.name().username() + "@email.com";
        String ownerEmail = faker.name().username() + "@email.com";

        Long providerId = insertServiceProvider(providerEmail);

        insertService("Nottingham Dog Grooming", 40.00, "NG9 1AL", providerId, 52.929114, -1.222497);

        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Test Owner");
        jdbcTemplate.update("UPDATE users SET location = 'NG5 1AA', latitude = 52.971209, longitude = -1.153513 WHERE id = ?", ownerId);

        driver.get("http://localhost:8081/services");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='service-list-card']")));

        String pageText = driver.findElement(By.cssSelector("body")).toString();
        driver.findElement(By.cssSelector("[data-testid='pet-care-filter']")).click();
        driver.findElement(By.cssSelector("select[data-testid='distance-filter'] > option[value='10'")).click();
        assertThat(pageText.contains("Nottingham Dog Grooming"));
        assertThat(pageText.contains("10.0 km away"));
    }
}



