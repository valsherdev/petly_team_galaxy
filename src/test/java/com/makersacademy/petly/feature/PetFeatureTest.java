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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class PetFeatureTest {
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
            jdbcTemplate.update("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
            jdbcTemplate.update("TRUNCATE TABLE pets RESTART IDENTITY CASCADE");
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

    private Long insertPetOwner(String username, String name) {
        jdbcTemplate.update(
                "INSERT INTO users (username, enabled, role, name) VALUES (?, true, 'PET_OWNER', ?)",
                username, name);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, username);
    }

    private void insertPet() {
        jdbcTemplate.update(
                "INSERT INTO pets (name, type, breed, age, description, photo, owner_id) " +
                        "VALUES ('Barney', 'Dog', 'Golden Retriever', 3, 'A test pet', 'url', 1)");
    }

    @Test
    public void ownerCanSeeNoPetsOnMyPetsPage() {

        String ownerEmail = faker.name().username() + "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "Test Owner");

        driver.get("http://localhost:8081/my-pets");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='add-a-pet-btn']")));

        String pageText = driver.findElement(By.tagName("body")).getText();
        assertTrue(pageText.contains("You haven't added any pets to your profile"));
    }

    @Test
    public void ownerCanAddAPetAndSeePetOnMyPetsPage() {

        String ownerEmail = faker.name().username() + "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "Test Owner");

        driver.get("http://localhost:8081/my-pets");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='add-a-pet-btn']")));

        driver.findElement(By.cssSelector("[data-testid='add-a-pet-btn']")).click();
        wait.until(ExpectedConditions.urlContains("/my-pets/add"));

        driver.findElement(By.cssSelector("[data-testid='name']")).sendKeys("Patrick");
        driver.findElement(By.cssSelector("[data-testid='type']")).sendKeys("Dog");
        driver.findElement(By.cssSelector("[data-testid='breed']")).sendKeys("Golden Retriever");
        driver.findElement(By.cssSelector("[data-testid='age']")).sendKeys("7");
        driver.findElement(By.cssSelector("[data-testid='description']")).sendKeys("I am a dog");

        driver.findElement(By.cssSelector("[data-testid='submit-pet-btn']")).click();

        wait.until(ExpectedConditions.urlContains("/my-pets"));

        String pageText = driver.findElement(By.cssSelector("[data-testid='pet-card']")).getText();
        assertTrue(pageText.contains("Patrick"));
    }

    @Test
    public void ownerCanEditExistingPetOnMyPetsProfilePage() {

        String ownerEmail = faker.name().username() + "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "Test Owner");
        insertPet();

        driver.get("http://localhost:8081/my-pets");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='add-another-pet-btn']")));

        driver.findElement(By.cssSelector("[data-testid='update-pet-btn']")).click();
        wait.until(ExpectedConditions.urlContains("/my-pets/1/edit"));



        driver.findElement(By.cssSelector("[data-testid='name-edit']")).sendKeys("Ben");
        driver.findElement(By.cssSelector("[data-testid='save-changes-btn']")).click();

        wait.until(ExpectedConditions.urlContains("/my-pets"));

        String pageText = driver.findElement(By.cssSelector("[data-testid='pet-card']")).getText();
        assertTrue(pageText.contains("Ben"));
    }

    @Test
    public void ownerCanRemoveExistingPetFromMyPetsPage() {

        String ownerEmail = faker.name().username() + "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "Test Owner");
        insertPet();

        driver.get("http://localhost:8081/my-pets");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='add-another-pet-btn']")));

        driver.findElement(By.cssSelector("[data-testid='remove-pet-btn']")).click();
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        boolean isDeleted = wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("[data-testid='pet-card']")));
        assertTrue(isDeleted);
    }
}
