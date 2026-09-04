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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class MessageFeatureTest {

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


    // helper methods:

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

    private void insertMessage(Long senderId, Long recipientId, String content, boolean read) {
        jdbcTemplate.update(
                "INSERT INTO messages (sender_id, recipient_id, content, read) VALUES (?, ?, ?, ?)",
                senderId, recipientId, content, read);
    }

    private int messageCountFor(Long senderId, Long recipientId, String content) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM messages WHERE sender_id = ? AND recipient_id = ? AND content = ?",
                Integer.class, senderId, recipientId, content);
    }

    private boolean isMessageRead(Long senderId, Long recipientId, String content) {
        return jdbcTemplate.queryForObject(
                "SELECT read FROM messages WHERE sender_id = ? AND recipient_id = ? AND content = ?",
                Boolean.class, senderId, recipientId, content);
    }


    // tests:

    @Test
    public void ownerCanSendMessageToProvider() {
        Long providerId = insertUser(faker.name().username() + "@email.com", "SERVICE_PROVIDER", "Test Provider");

        String ownerEmail = faker.name().username() + "@email.com";
        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Test Owner");

        String content = "Hello, is this service available? " + faker.number().digits(6);

        driver.get("http://localhost:8081/messages/" + providerId);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("content")));

        driver.findElement(By.name("content")).sendKeys(content);
        driver.findElement(By.cssSelector("form[action^='/messages/'] button[type='submit']")).click();

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(1, messageCountFor(ownerId, providerId, content)));
    }

    @Test
    public void viewingConversationMarksItAsRead() {
        Long providerId = insertUser(faker.name().username() + "@email.com", "SERVICE_PROVIDER", "Test Provider");

        String ownerEmail = faker.name().username() + "@email.com";
        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Test Owner");

        String content = "Yes, we have availability " + faker.number().digits(6);
        insertMessage(providerId, ownerId, content, false);

        driver.get("http://localhost:8081/messages/" + providerId);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("content")));

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertTrue(isMessageRead(providerId, ownerId, content)));
    }

    @Test
    public void inboxShowsConversationWithPartnerName() {
        String partnerName = "Test Provider " + faker.number().digits(6);
        Long providerId = insertUser(faker.name().username() + "@email.com", "SERVICE_PROVIDER", partnerName);

        String ownerEmail = faker.name().username() + "@email.com";
        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Test Owner");

        insertMessage(ownerId, providerId, "Hi there!", false);

        driver.get("http://localhost:8081/messages");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains(partnerName));
    }


}
