package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** One login scenario per SauceDemo user type. */
public class DataDrivenTests extends BaseTest {

    @DataProvider(name = "loginData")
    public Object[][] loginData() {
        return new Object[][]{
                {"standard_user", "secret_sauce", true},
                {"locked_out_user", "secret_sauce", false},
                {"problem_user", "secret_sauce", true},
                {"performance_glitch_user", "secret_sauce", true},
                {"error_user", "secret_sauce", true},
                {"visual_user", "secret_sauce", true}
        };
    }

    @Test(dataProvider = "loginData")
    public void testLoginDataDriven(String username, String password, boolean shouldSucceed) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, password);

        if (shouldSucceed) {
            waitForUrl("inventory", 20); // performance_glitch_user is slow on purpose
            Assert.assertTrue(driver.getCurrentUrl().contains("inventory"), username + " should reach the inventory page");
        } else {
            String error = loginPage.getErrorMessage();
            Assert.assertTrue(error.contains("locked out"), username + " should be locked out, got: " + error);
        }
    }
}
