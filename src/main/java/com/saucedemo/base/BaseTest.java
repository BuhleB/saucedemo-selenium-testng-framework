package com.saucedemo.base;

import com.saucedemo.pages.LoginPage;
import com.saucedemo.pages.ProductsPage;
import com.saucedemo.utils.DriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.time.Duration;
import java.util.function.Function;

/**
 * Starts a fresh browser on the SauceDemo login page before every test and closes it afterwards.
 * Contains no reporting logic: results and failure screenshots are handled by TestListener.
 */
public class BaseTest {

    protected static final String BASE_URL = "https://www.saucedemo.com/";

    protected WebDriver driver;
    protected String browserName;

    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser) {
        browserName = browser;
        driver = DriverManager.getDriver(browser);
        driver.get(BASE_URL);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }

    /** Fails the test with a clear message if the condition is not met in time. */
    protected void waitForCondition(Function<WebDriver, Boolean> condition, int timeoutSeconds, String description) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .withMessage("Timed out waiting for: " + description)
                .until(condition);
    }

    protected void waitForUrl(String fragment) {
        waitForUrl(fragment, 10);
    }

    protected void waitForUrl(String fragment, int timeoutSeconds) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .withMessage("Timed out waiting for a URL containing '" + fragment + "'")
                .until(ExpectedConditions.urlContains(fragment));
    }

    /** Logs in and waits for the inventory page. Use LoginPage directly for failed-login scenarios. */
    protected ProductsPage loginAs(String username, String password) {
        new LoginPage(driver).login(username, password);
        waitForUrl("inventory");
        return new ProductsPage(driver);
    }
}
