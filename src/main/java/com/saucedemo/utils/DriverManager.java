package com.saucedemo.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates and stores one WebDriver per thread.
 * The browser name comes from the TestNG "browser" parameter (see the suite XML files).
 * Drivers are located or downloaded automatically by Selenium Manager (built into Selenium 4).
 * Run headless with -Dheadless=true.
 */
public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static WebDriver getDriver(String browser) {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            driver = createDriver(browser);
            DRIVER.set(driver);
        }
        return driver;
    }

    /** The driver already created for the current thread, or null. */
    public static WebDriver getDriver() {
        return DRIVER.get();
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.out.println("Warning: error while closing the browser: " + e.getMessage());
            }
            DRIVER.remove();
        }
    }

    private static boolean isHeadless() {
        return Boolean.parseBoolean(System.getProperty("headless", "false"));
    }

    private static WebDriver createDriver(String browser) {
        WebDriver driver;
        switch (browser.toLowerCase()) {
            case "chrome":
                driver = createChrome();
                break;
            case "firefox":
                driver = createFirefox();
                break;
            case "edge":
                driver = createEdge();
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported browser '" + browser + "'. Use chrome, firefox or edge.");
        }
        configure(driver);
        return driver;
    }

    /** Stops the browser's own "save / check your password" pop-ups from covering the page. */
    private static Map<String, Object> noPasswordPrompts() {
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        return prefs;
    }

    private static WebDriver createChrome() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*", "--disable-notifications", "--disable-dev-shm-usage");
        options.setExperimentalOption("prefs", noPasswordPrompts());
        if (isHeadless()) {
            options.addArguments("--headless=new", "--window-size=1920,1080", "--no-sandbox");
        }
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefox() {
        FirefoxOptions options = new FirefoxOptions();
        if (isHeadless()) {
            options.addArguments("-headless", "--width=1920", "--height=1080");
        }
        return new FirefoxDriver(options);
    }

    private static WebDriver createEdge() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--remote-allow-origins=*", "--disable-notifications", "--disable-dev-shm-usage");
        options.setExperimentalOption("prefs", noPasswordPrompts());
        if (isHeadless()) {
            options.addArguments("--headless=new", "--window-size=1920,1080", "--no-sandbox");
        }
        return new EdgeDriver(options);
    }

    private static void configure(WebDriver driver) {
        if (!isHeadless()) {
            driver.manage().window().maximize();
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10));
    }
}
