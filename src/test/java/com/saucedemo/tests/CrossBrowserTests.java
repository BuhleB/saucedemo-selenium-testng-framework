package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.pages.CartPage;
import com.saucedemo.pages.CheckoutPage;
import com.saucedemo.pages.LoginPage;
import com.saucedemo.pages.ProductsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * The core user journeys, run once per browser by testng-sequential-browsers.xml
 * (Chrome, then Firefox, then Edge). Every check is a real assertion.
 */
public class CrossBrowserTests extends BaseTest {

    @Test(priority = 1)
    public void testInvalidLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("invalid_user", "invalid_password");
        String error = loginPage.getErrorMessage();
        Assert.assertTrue(error.contains("Username and password do not match"), "Wrong error message: " + error);
    }

    @Test(priority = 2)
    public void testValidLogin() {
        ProductsPage productsPage = loginAs("standard_user", "secret_sauce");
        Assert.assertTrue(productsPage.isProductsPageDisplayed(), "Products page should be displayed after login");
    }

    @Test(priority = 3)
    public void testCompletePurchaseFlow() {
        ProductsPage productsPage = loginAs("standard_user", "secret_sauce");
        productsPage.addItemToCart(0);
        productsPage.addItemToCart(1);
        Assert.assertEquals(productsPage.getCartBadgeCount(), "2");

        productsPage.clickCartIcon();
        waitForUrl("cart.html");
        CartPage cartPage = new CartPage(driver);
        Assert.assertEquals(cartPage.getNumberOfItemsInCart(), 2);

        cartPage.clickCheckout();
        waitForUrl("checkout-step-one");
        CheckoutPage checkoutPage = new CheckoutPage(driver);
        checkoutPage.fillInformation("John", "Doe", "12345");
        checkoutPage.clickContinue();

        waitForUrl("checkout-step-two");
        checkoutPage.finishCheckout();
        waitForUrl("checkout-complete");
        Assert.assertEquals(checkoutPage.getSuccessMessage(), "Thank you for your order!");
    }

    @Test(priority = 4)
    public void testLogout() {
        ProductsPage productsPage = loginAs("standard_user", "secret_sauce");
        productsPage.logout();
        waitForCondition(d -> new LoginPage(d).isLoginPageDisplayed(), 10, "login page after logout");
        Assert.assertTrue(new LoginPage(driver).isLoginPageDisplayed(), "Should be back on the login page");
    }

    @Test(priority = 5)
    public void testAddRemoveItemsFromCart() {
        ProductsPage productsPage = loginAs("standard_user", "secret_sauce");
        productsPage.addItemToCart(0);
        productsPage.addItemToCart(1);
        Assert.assertEquals(productsPage.getCartBadgeCount(), "2");

        productsPage.clickCartIcon();
        waitForUrl("cart.html");
        CartPage cartPage = new CartPage(driver);
        cartPage.removeItem(0);
        waitForCondition(d -> cartPage.getNumberOfItemsInCart() == 1, 5, "one item left in the cart");
        Assert.assertEquals(cartPage.getNumberOfItemsInCart(), 1, "One item should remain in the cart");

        cartPage.clickContinueShopping();
        waitForUrl("inventory");
        Assert.assertEquals(new ProductsPage(driver).getCartBadgeCount(), "1");
    }
}
