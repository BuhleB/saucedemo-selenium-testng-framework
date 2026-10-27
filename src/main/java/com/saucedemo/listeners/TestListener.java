package com.saucedemo.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.saucedemo.utils.DriverManager;
import com.saucedemo.utils.ExtentReportManager;
import org.testng.IConfigurationListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Single source of truth for reporting. Registered through
 * META-INF/services/org.testng.ITestNGListener, so it applies to every suite.
 */
public class TestListener implements ITestListener, ISuiteListener, IConfigurationListener {

    private static final String STARTED = "extentStarted";

    @Override
    public void onStart(ISuite suite) {
        ExtentReportManager.getInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        startEntry(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentReportManager.currentTest();
        if (test != null) {
            test.pass("Test passed");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentReportManager.currentTest();
        if (test == null) {
            return;
        }
        test.fail(result.getThrowable());
        // onTestFailure runs before @AfterMethod, so the browser is still open here.
        ExtentReportManager.attachFailureScreenshot(
                test, DriverManager.getDriver(), result.getMethod().getMethodName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // A skip caused by a failed @BeforeMethod never goes through onTestStart.
        if (result.getAttribute(STARTED) == null) {
            startEntry(result);
        }
        ExtentTest test = ExtentReportManager.currentTest();
        if (test == null) {
            return;
        }
        if (result.getThrowable() != null) {
            test.skip(result.getThrowable());
        } else {
            test.skip("Test skipped");
        }
    }

    /** A failed @BeforeMethod (for example a browser that cannot start) would otherwise only show as skipped tests. */
    @Override
    public void onConfigurationFailure(ITestResult result) {
        String browser = browserOf(result);
        ExtentTest entry = ExtentReportManager.startTest(
                "Setup failed: " + result.getMethod().getMethodName() + " [" + browser.toUpperCase() + "]", browser);
        entry.fail(result.getThrowable());
    }

    @Override
    public void onFinish(ISuite suite) {
        ExtentReportManager.flush();
        ExtentReportManager.openReportIfRequested();
    }

    private void startEntry(ITestResult result) {
        result.setAttribute(STARTED, Boolean.TRUE);
        String browser = browserOf(result);
        StringBuilder name = new StringBuilder(result.getMethod().getMethodName());
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0 && parameters[0] instanceof String) {
            name.append(" (").append(parameters[0]).append(")");
        }
        name.append(" [").append(browser.toUpperCase()).append("]");
        ExtentReportManager.startTest(name.toString(), browser);
    }

    private String browserOf(ITestResult result) {
        String browser = result.getTestContext().getCurrentXmlTest().getParameter("browser");
        return browser == null ? "chrome" : browser;
    }
}
