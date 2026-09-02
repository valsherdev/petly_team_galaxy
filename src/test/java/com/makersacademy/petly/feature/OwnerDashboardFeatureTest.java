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
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class OwnerDashboardFeatureTest {

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

            String dashboardUrl = "PET_OWNER".equals(role)
                    ? "http://localhost:8081/dashboard/owner"
                    : "http://localhost:8081/dashboard/provider";
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

        private Long insertService(String name, Double price, String providerLocation, Long providerId) {
            jdbcTemplate.update(
                    "INSERT INTO services (name, type, price, price_unit, location, description, provider_id, duration, latitude, longitude) " +
                            "VALUES (?, 'PET_CARE', ?, 'PER_HOUR', ?, 'A test service description', ?, 60, 53.500452, -2.204787)",
                    name, price, providerLocation, providerId);

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

    private Long insertServiceProvider(String username, String name) {
        jdbcTemplate.update(
                "INSERT INTO users (username, enabled, role, name, location) VALUES (?, true, 'SERVICE_PROVIDER', ?, 'M40 8BZ')",
                username, name);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, username);
    }

    @Test
    @DisplayName("Displays welcome header and owner account details correctly")
    public void testDashboardHeaderAndAccountDetails() {
        String ownerEmail = faker.name().username() + "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "Alex Owner");

        jdbcTemplate.update("UPDATE users SET location = 'SW1A 1AA' WHERE username = ?", ownerEmail);
        driver.navigate().refresh();

        WebElement welcomeMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='welcome_message']")));
        assertTrue(welcomeMessage.getText().contains("Welcome back, Alex Owner!"));

        WebElement nameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='account_name']")));
        WebElement locationElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='account_location']")));

        assertEquals("Alex Owner", nameElement.getText());
        assertEquals("SW1A 1AA", locationElement.getText());
    }

    @Test
    @DisplayName("Displays owner's pets on the dashboard")
    public void testDashboardPetsList() {
        String ownerEmail = faker.name().username() + "@email.com";
        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Owner");

        insertPet("Barney", "Dog", ownerId);
        insertPet("Milo", "Cat", ownerId);

        driver.navigate().refresh();
        driver.findElement(By.cssSelector("[data-testid='my_pets']"));

        assertTrue(driver.getPageSource().contains("Barney"));
        assertTrue(driver.getPageSource().contains("Milo"));
    }

    @Test
    @DisplayName("Displays message when no pets or bookings exist")
    public void testDashboardEmptyStates() {
        String ownerEmail = faker.name().username() + "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "New Owner");

        driver.findElement(By.cssSelector("[data-testid='my_pets']"));
        driver.findElement(By.cssSelector("[data-testid='my_bookings']"));

        assertTrue(driver.getPageSource().contains("No pets added."));
        assertTrue(driver.getPageSource().contains("No active bookings."));
    }

    @Test
    @DisplayName("Displays owner's active bookings on dashboard")
    public void testOwnerDashboardBookings() {
        String ownerEmail = faker.name().username() + "@email.com";
        String providerEmail = faker.name().username() + "@email.com";

        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Alex Owner");

        Long providerId = insertServiceProvider(providerEmail, "Jane Provider");
        Long petId = insertPet("Barney", "Dog", ownerId);
        Long serviceId = insertService("Dog grooming", 35.00, "SW1A 1AA", providerId);

        insertBooking(petId, serviceId, ownerId, providerId, "CONFIRMED");

        driver.navigate().refresh();
        driver.findElement(By.cssSelector("[data-testid='my_bookings']"));

        assertTrue(driver.getPageSource().contains("Dog grooming"));
        assertTrue(driver.getPageSource().contains("Confirmed"));
    }

    @Test
    @DisplayName("Displays active bookings and not cancelled bookings")
    public void testDashboardBookings() {
        String ownerEmail = faker.name().username() + "@email.com";
        String providerEmail = faker.name().username() + "@email.com";

        Long ownerId = signUpAs(ownerEmail, "PET_OWNER", "Owner");
        Long providerId = insertServiceProvider(providerEmail, "Pet Care Co.");
        Long petId = insertPet("Barney", "Dog", ownerId);

        Long activeService = insertService("Dog walking", 25.00, "SW1A 1AA", providerId);
        Long cancelledService = insertService("Pet sitting", 40.00, "SW1A 1AA", providerId);

        insertBooking(petId, activeService, ownerId, providerId, "CONFIRMED");
        insertBooking(petId, cancelledService, ownerId, providerId, "CANCELLED");

        driver.navigate().refresh();
        driver.findElement(By.cssSelector("[data-testid='my_bookings']"));

        String pageSource = driver.getPageSource();
        assertFalse(pageSource.contains("Cancelled"));

        assertTrue(driver.getPageSource().contains("Dog walking"));
        assertTrue(driver.getPageSource().contains("Confirmed"));
    }

    @Test
    @DisplayName("Displays available services with pricing")
    public void testDashboardServicesList() {
        String ownerEmail = faker.name().username()+ "@email.com";
        String providerEmail = faker.name().username() + "@email.com";

        signUpAs(ownerEmail, "PET_OWNER", "Alex Owner");
        Long providerId = insertServiceProvider(providerEmail, "Paws care");

        insertService("Dog grooming", 45.00, "SW1A 1AA", providerId);
        insertService("Overnight pet sitting", 60.00, "SW1A 1AA", providerId);

        driver.navigate().refresh();
        driver.findElement(By.cssSelector("[data-testid='services']"));

        assertTrue(driver.getPageSource().contains("Dog grooming"));
        assertTrue(driver.getPageSource().contains("Overnight pet sitting"));
        assertTrue(driver.getPageSource().contains("£45"));
        assertTrue(driver.getPageSource().contains("£60"));
    }

    @Test
    @DisplayName("Navigates to the add pet form when clicking Add a Pet button")
    public void testAddPetButtonNavigatesToAddPetForm() {
        String ownerEmail = faker.name().username().toLowerCase() + "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "Alex Owner");

        driver.navigate().refresh();

        driver.findElement(By.cssSelector("[data-testid='add-a-pet-btn']")).click();

        assertTrue(driver.getCurrentUrl().contains("/my-pets/add"));
        assertTrue(driver.getPageSource().contains("Add Pet Profile"));
    }

    @Test
    @DisplayName("Navigates to the bookings page when clicking My Bookings")
    public void testMyBookingsButtonNavigatesToBookingsPage() {
        String ownerEmail = faker.name().username().toLowerCase() + "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "Alex Owner");

        driver.navigate().refresh();

        driver.findElement(By.cssSelector("[data-testid='view-bookings-btn']")).click();

        assertTrue(driver.getCurrentUrl().contains("/dashboard/owner/bookings"));
        assertTrue(driver.getPageSource().contains("My Bookings"));
    }

    @Test
    @DisplayName("Navigates to the services page when clicking Browse Services")
    public void testExploreServiceButtonNavigatesToServicesPage() {
        String ownerEmail = faker.name().username().toLowerCase() + "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "Alex Owner");

        driver.navigate().refresh();

        driver.findElement(By.cssSelector("[data-testid='explore-services-btn']")).click();

        assertTrue(driver.getCurrentUrl().contains("/services"));
        assertTrue(driver.getPageSource().contains("Browse Services"));
    }

    @Test
    @DisplayName("Navigates to the profile page when clicking Manage Profile")
    public void testManageProfileButtonNavigatesToProfilePage() {
        String ownerEmail = faker.name().username().toLowerCase() + "@email.com";
        signUpAs(ownerEmail, "PET_OWNER", "Alex Owner");

        driver.navigate().refresh();

        driver.findElement(By.cssSelector("[data-testid='manage-profile-btn']")).click();

        assertTrue(driver.getCurrentUrl().contains("/profile"));
        assertTrue(driver.getPageSource().contains("My Profile"));
    }
}

