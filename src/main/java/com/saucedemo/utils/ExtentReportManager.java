package com.saucedemo.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Owns the single ExtentReports instance for the run. Only TestListener talks to it;
 * test classes contain no reporting code.
 */
public final class ExtentReportManager {

    private static final String REPORT_DIR = "test-output/ExtentReports";
    private static final String SCREENSHOT_DIR = REPORT_DIR + "/screenshots";
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

    private static ExtentReports extent;
    private static String reportPath;

    private ExtentReportManager() {
    }

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            new File(SCREENSHOT_DIR).mkdirs();
            reportPath = REPORT_DIR + "/TestReport_"
                    + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".html";

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setDocumentTitle("SauceDemo Automation Report");
            spark.config().setReportName("SauceDemo Test Execution Report");
            spark.config().setEncoding("utf-8");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Application", "https://www.saucedemo.com");
            extent.setSystemInfo("Stack", "Selenium WebDriver 4, TestNG, Maven");
            extent.setSystemInfo("Java", System.getProperty("java.version"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Headless", System.getProperty("headless", "false"));
        }
        return extent;
    }

    /** Creates the report entry for a test and makes it the current test of this thread. */
    public static synchronized ExtentTest startTest(String name, String browser) {
        ExtentTest test = getInstance().createTest(name);
        test.assignCategory(browser.toUpperCase());
        CURRENT_TEST.set(test);
        return test;
    }

    public static ExtentTest currentTest() {
        return CURRENT_TEST.get();
    }

    /** Saves a screenshot next to the report and attaches it to the given test. */
    public static void attachFailureScreenshot(ExtentTest test, WebDriver driver, String testName) {
        if (test == null || driver == null) {
            return;
        }
        try {
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String fileName = testName.replaceAll("[^a-zA-Z0-9_-]", "_") + "_"
                    + new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date()) + ".png";
            new File(SCREENSHOT_DIR).mkdirs();
            Files.copy(source.toPath(), new File(SCREENSHOT_DIR, fileName).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            // Path is relative to the report file, which sits in REPORT_DIR.
            test.addScreenCaptureFromPath("screenshots/" + fileName);
        } catch (Exception e) {
            test.warning("Could not capture screenshot: " + e.getMessage());
        }
    }

    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
            System.out.println("Extent report: " + new File(reportPath).getAbsolutePath());
        }
    }

    /** Opens the finished report in the default browser when run with -DopenReport=true. */
    public static synchronized void openReportIfRequested() {
        if (reportPath == null || !Boolean.getBoolean("openReport")) {
            return;
        }
        try {
            if (!GraphicsEnvironment.isHeadless() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new File(reportPath).toURI());
            }
        } catch (Exception e) {
            System.out.println("Could not open the report automatically: " + e.getMessage());
        }
    }
}
