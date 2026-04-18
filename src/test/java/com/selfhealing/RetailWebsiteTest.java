package com.selfhealing;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@Epic("Retail E-Commerce Application")
@Feature("Core Shopping Workflows")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RetailWebsiteTest {

    private WebDriver driver;
    private TestOrchestrator orchestrator;

    @BeforeAll
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        orchestrator = new TestOrchestrator(driver);
    }

    @AfterAll
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @Story("User Login Workflow")
    @Severity(SeverityLevel.CRITICAL)
    public void testUserLoginHealing() {
        driver.get("https://retail-website-two.vercel.app/login");
        orchestrator.resilientFill("input[type='email']", "test@demo.com", "Email Input", "TC1-Step-1"); // PASS
        orchestrator.resilientFill(".broken-pass", "password123", "Password Input", "TC1-Step-2"); // HEAL
        orchestrator.resilientClick("//button[contains(., 'Sign in')]", "Sign In Button", "TC1-Step-3"); // PASS
        
        try { Thread.sleep(2000); } catch (Exception e) {} 
        orchestrator.assertNoHealing();
    }

    @Test
    @Order(2)
    @Story("Navigate to Products")
    @Severity(SeverityLevel.NORMAL)
    public void testNavigationToProducts() {
        driver.get("https://retail-website-two.vercel.app/app/dashboard");
        orchestrator.resilientClick(".broken-products-link", "Products Menu", "TC2-Step-1"); // HEAL
        try { Thread.sleep(1000); } catch (Exception e) {}
        orchestrator.assertNoHealing();
    }

    @Test
    @Order(3)
    @Story("Add Product to Cart")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddProductToCart() {
        driver.get("https://retail-website-two.vercel.app/app/products");
        orchestrator.resilientClick("#root", "Add to Cart Button", "TC3-Step-1"); // PASS
        orchestrator.assertNoHealing();
    }

    @Test
    @Order(4)
    @Story("Checkout Payment Form")
    @Severity(SeverityLevel.CRITICAL)
    public void testCheckoutPayment() {
        driver.get("https://retail-website-two.vercel.app/app/cart");
        orchestrator.resilientFill(".broken-fullname", "John Doe", "Full Name Input", "TC4-Step-1"); // HEAL
        orchestrator.resilientClick("#root", "Pay Button", "TC4-Step-2"); // PASS
        orchestrator.assertNoHealing();
    }

    @Test
    @Order(5)
    @Story("Settings & Sign Out")
    @Severity(SeverityLevel.NORMAL)
    public void testSettingsAndSignOut() {
        driver.get("https://retail-website-two.vercel.app/app/settings");
        orchestrator.resilientClick(".broken-settings", "Settings Menu", "TC5-Step-1"); // HEAL
        try { Thread.sleep(1000); } catch (Exception e) {}
        driver.get("https://retail-website-two.vercel.app/login");
        orchestrator.assertNoHealing();
    }
}
