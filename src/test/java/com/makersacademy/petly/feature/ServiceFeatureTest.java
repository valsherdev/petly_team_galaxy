package com.makersacademy.petly.feature;


import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class ServiceFeatureTest {

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


    private Long insertProvider(String username, String name) {
        jdbcTemplate.update(
                "INSERT INTO users (username, enabled, role, name) VALUES (?, true, 'SERVICE_PROVIDER', ?)",
                username, name);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, username);
    }

    private Long insertService(Long providerId, String name, String type,
                               String priceUnit, String description) {
        jdbcTemplate.update(
                "INSERT INTO services (name, type, price, price_unit, description, provider_id) " +
                        "VALUES (?, ?, 25.00, ?, ?, ?)",
                name, type, priceUnit, description, providerId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM services WHERE name = ? AND provider_id = ?",
                Long.class, name, providerId);
    }

    private int serviceCountFor(Long providerId, String name) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM services WHERE provider_id = ? AND name = ?",
                Integer.class, providerId, name);
    }

    private String serviceNameFor(Long serviceId) {
        return jdbcTemplate.queryForObject(
                "SELECT name FROM services WHERE id = ?", String.class, serviceId);
    }


    @Test
    public void providerCanCreateService() {
        String email = faker.name().username() + "@email.com";
        signUpAs(email, "SERVICE_PROVIDER", "Test Provider");

        String serviceName = "Feature Test Grooming " + faker.number().digits(6);


        String createFormSelector = "form[action='/services/create'] ";

        driver.findElement(By.cssSelector(createFormSelector + "input[name='name']")).sendKeys(serviceName);

        new org.openqa.selenium.support.ui.Select(
                driver.findElement(By.cssSelector(createFormSelector + "select[name='type']")))
                .selectByValue("GROOMING");

        driver.findElement(By.cssSelector(createFormSelector + "input[name='price']")).sendKeys("35.00");

        new org.openqa.selenium.support.ui.Select(
                driver.findElement(By.cssSelector(createFormSelector + "select[name='priceUnit']")))
                .selectByValue("FIXED");

        driver.findElement(By.cssSelector(createFormSelector + "textarea[name='description']")).sendKeys("A test grooming service");
        driver.findElement(By.cssSelector(createFormSelector + "input[name='location']")).sendKeys("SW1A 1AA");
        driver.findElement(By.cssSelector(createFormSelector + "button[type='submit']")).click();

        Long providerId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, email);

        await().atMost(5, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(1, serviceCountFor(providerId, serviceName)));
    }


    @Test
    public void providerCanDeleteTheirService() {
        String email = faker.name().username() + "@email.com";
        Long providerId = signUpAs(email, "SERVICE_PROVIDER", "Test Provider");

        String serviceName = "Service To Delete " + faker.number().digits(6);
        Long serviceId = insertService(providerId, serviceName, "VETERINARY", "PER_HOUR", "Will be deleted");

        driver.get("http://localhost:8081/dashboard/provider");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

        driver.findElement(
                By.cssSelector("form[action='/services/" + serviceId + "/delete'] button")).click();

        driver.switchTo().alert().accept();

        await().atMost(5, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(0, serviceCountFor(providerId, serviceName)));
    }


    @Test
    public void ownerCanSeeExistingServiceOnBrowsePage() {
        Long providerId = insertProvider(faker.name().username() + "@email.com", "Happy Paws");
        String serviceName = "Seeded Dog Walking " + faker.number().digits(6);
        insertService(providerId, serviceName, "PET_CARE", "PER_HOUR", "A seeded service for testing");

        String ownerEmail = faker.name().username() + "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "Test Owner");

        driver.get("http://localhost:8081/services");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains(serviceName));
    }

    @Test
    public void providerCanEditTheirOwnService() {
        String email = faker.name().username() + "@email.com";
        Long providerId = signUpAs(email, "SERVICE_PROVIDER", "Test Provider");

        String originalName = "Original Name " + faker.number().digits(6);
        Long serviceId = insertService(providerId, originalName, "GROOMING", "FIXED", "Before edit");

        driver.get("http://localhost:8081/dashboard/provider");
        driver.findElement(By.cssSelector("a[href='/services/" + serviceId + "/edit']")).click();

        String updateFormSelector = "form[action='/services/" + serviceId + "/update'] ";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(updateFormSelector + "input[name='name']")));

        String updatedName = "Updated Name " + faker.number().digits(6);
        org.openqa.selenium.WebElement nameField = driver.findElement(By.cssSelector(updateFormSelector + "input[name='name']"));
        nameField.clear();
        nameField.sendKeys(updatedName);

        org.openqa.selenium.WebElement locationField = driver.findElement(By.cssSelector(updateFormSelector + "input[name='location']"));
        locationField.clear();
        locationField.sendKeys("SW1A 1AA");

        driver.findElement(By.cssSelector(updateFormSelector + "button[type='submit']")).click();

        await().atMost(5, java.util.concurrent.TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(updatedName, serviceNameFor(serviceId)));
    }


    @Test
    public void providerCannotEditAnotherProvidersService() {
        String email = faker.name().username() + "@email.com";
        signUpAs(email, "SERVICE_PROVIDER", "Provider A");

        Long otherProviderId = insertProvider(faker.name().username() + "@email.com", "Provider B");
        String originalName = "Provider B's Service " + faker.number().digits(6);
        Long otherServiceId = insertService(otherProviderId, originalName, "VETERINARY", "PER_HOUR", "Not Provider A's");

        
        driver.get("http://localhost:8081/services/" + otherServiceId + "/edit");

        wait.until(ExpectedConditions.urlContains("/dashboard/provider"));
        assertEquals(originalName, serviceNameFor(otherServiceId));
    }
}