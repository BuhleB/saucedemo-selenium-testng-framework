package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.pages.LoginPage;
import com.saucedemo.pages.ProductsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoginTests extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod
    public void createPages() {
        loginPage = new LoginPage(driver);
    }

    @Test(priority = 1)
    public void testSuccessfulLogin() {
        loginPage.login("standard_user", "secret_sauce");
        waitForUrl("inventory");
        Assert.assertEquals(new ProductsPage(driver).getPageTitle(), "Products");
    }

    @Test(priority = 2)
    public void testLockedOutUser() {
        loginPage.login("locked_out_user", "secret_sauce");
        String error = loginPage.getErrorMessage();
        Assert.assertTrue(error.contains("Sorry, this user has been locked out"), "Wrong error message: " + error);
        Assert.assertFalse(driver.getCurrentUrl().contains("inventory"), "Locked-out user must stay on the login page");
    }

    @Test(priority = 3)
    public void testInvalidPassword() {
        loginPage.login("standard_user", "wrong_password");
        String error = loginPage.getErrorMessage();
        Assert.assertTrue(error.contains("Username and password do not match"), "Wrong error message: " + error);
    }

    @Test(priority = 4)
    public void testEmptyUsername() {
        loginPage.login("", "secret_sauce");
        String error = loginPage.getErrorMessage();
        Assert.assertTrue(error.contains("Username is required"), "Wrong error message: " + error);
    }

    @Test(priority = 5)
    public void testEmptyPassword() {
        loginPage.login("standard_user", "");
        String error = loginPage.getErrorMessage();
        Assert.assertTrue(error.contains("Password is required"), "Wrong error message: " + error);
    }

    @Test(priority = 6)
    public void testPerformanceGlitchUser() {
        loginPage.login("performance_glitch_user", "secret_sauce");
        // This user's login is deliberately slow, so allow extra time.
        waitForUrl("inventory", 20);
        Assert.assertEquals(new ProductsPage(driver).getPageTitle(), "Products");
    }

    @Test(priority = 7)
    public void testProblemUser() {
        loginPage.login("problem_user", "secret_sauce");
        waitForUrl("inventory");
        Assert.assertEquals(new ProductsPage(driver).getPageTitle(), "Products");
    }

    @Test(priority = 8)
    public void testLoginFormFieldsAcceptInput() {
        loginPage.enterUsername("some_user");
        loginPage.enterPassword("some_password");
        Assert.assertEquals(loginPage.getUsernameValue(), "some_user");
        Assert.assertEquals(loginPage.getPasswordValue(), "some_password");
        Assert.assertEquals(loginPage.getPasswordFieldType(), "password", "Password must be masked");
    }

    @Test(priority = 9)
    public void testLoginButtonState() {
        Assert.assertTrue(loginPage.isLoginButtonEnabled(), "Login button should be visible and enabled");
        Assert.assertEquals(loginPage.getLoginButtonLabel(), "Login");
    }

    @Test(priority = 10)
    public void testMultipleFailedLoginAttempts() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            driver.navigate().refresh(); // start each attempt from a clean form and no old error
            loginPage.login("invalid" + attempt, "invalid" + attempt);
            String error = loginPage.getErrorMessage();
            Assert.assertTrue(error.contains("Username and password do not match"),
                    "Attempt " + attempt + " gave the wrong error: " + error);
        }
        Assert.assertFalse(driver.getCurrentUrl().contains("inventory"), "No failed attempt may log the user in");
    }
}
