package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.pages.CartPage;
import com.saucedemo.pages.LoginPage;
import com.saucedemo.pages.ProductsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Quick critical-path checks: if any of these fail, the rest of the suite is not worth running. */
public class SmokeTests extends BaseTest {

    @Test(priority = 1)
    public void testLoginLogout() {
        ProductsPage productsPage = loginAs("standard_user", "secret_sauce");
        Assert.assertEquals(productsPage.getPageTitle(), "Products");

        productsPage.logout();
        waitForCondition(d -> new LoginPage(d).isLoginPageDisplayed(), 10, "login page after logout");
        Assert.assertFalse(driver.getCurrentUrl().contains("inventory"), "Logout failed. URL: " + driver.getCurrentUrl());
    }

    @Test(priority = 2)
    public void testHomepageLoading() {
        ProductsPage productsPage = loginAs("standard_user", "secret_sauce");
        Assert.assertEquals(productsPage.getInventoryItemCount(), 6, "Should have exactly 6 products");
    }

    @Test(priority = 3)
    public void testAddToCart() {
        ProductsPage productsPage = loginAs("standard_user", "secret_sauce");
        productsPage.addItemToCart(0);
        Assert.assertEquals(productsPage.getCartBadgeCount(), "1");
    }

    @Test(priority = 4)
    public void testCheckoutPageNavigation() {
        ProductsPage productsPage = loginAs("standard_user", "secret_sauce");
        productsPage.addItemToCart(0);
        productsPage.clickCartIcon();
        waitForUrl("cart.html");

        new CartPage(driver).clickCheckout();
        waitForUrl("checkout-step-one");
        Assert.assertTrue(driver.getCurrentUrl().contains("checkout-step-one"));
    }

    @Test(priority = 5)
    public void testCartPageFunctionality() {
        ProductsPage productsPage = loginAs("standard_user", "secret_sauce");
        productsPage.addItemToCart(0);
        productsPage.addItemToCart(1);
        Assert.assertEquals(productsPage.getCartBadgeCount(), "2");

        productsPage.clickCartIcon();
        waitForUrl("cart.html");
        Assert.assertEquals(new CartPage(driver).getNumberOfItemsInCart(), 2);
    }

    @Test(priority = 6)
    public void testInvalidLoginError() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("invalid_user", "invalid_pass");
        String error = loginPage.getErrorMessage();
        Assert.assertTrue(error.contains("Username and password do not match"), "Wrong error message: " + error);
    }

    @Test(priority = 7)
    public void testBackToProductsNavigation() {
        ProductsPage productsPage = loginAs("standard_user", "secret_sauce");
        productsPage.clickCartIcon();
        waitForUrl("cart.html");

        new CartPage(driver).clickContinueShopping();
        waitForUrl("inventory");
        Assert.assertEquals(new ProductsPage(driver).getPageTitle(), "Products");
    }
}
