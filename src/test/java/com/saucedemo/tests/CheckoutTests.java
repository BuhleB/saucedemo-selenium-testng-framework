package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.pages.CartPage;
import com.saucedemo.pages.CheckoutPage;
import com.saucedemo.pages.ProductsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CheckoutTests extends BaseTest {

    private CartPage cartPage;
    private CheckoutPage checkoutPage;

    /** Every test starts on the cart page with one item in the cart. */
    @BeforeMethod
    public void loginAndAddProduct() {
        ProductsPage productsPage = loginAs("standard_user", "secret_sauce");
        productsPage.addItemToCart(0);
        driver.get(BASE_URL + "cart.html");
        waitForUrl("cart.html");
        cartPage = new CartPage(driver);
        checkoutPage = new CheckoutPage(driver);
    }

    private void openCheckoutAndSubmit(String firstName, String lastName, String postalCode) {
        cartPage.clickCheckout();
        waitForUrl("checkout-step-one");
        checkoutPage.fillInformation(firstName, lastName, postalCode);
        checkoutPage.clickContinue();
    }

    private void finishOrderAndAssertConfirmation() {
        waitForUrl("checkout-step-two");
        checkoutPage.finishCheckout();
        waitForUrl("checkout-complete");
        Assert.assertEquals(checkoutPage.getSuccessMessage(), "Thank you for your order!");
    }

    @Test(priority = 1)
    public void testSuccessfulCheckout() {
        openCheckoutAndSubmit("John", "Doe", "12345");
        finishOrderAndAssertConfirmation();
    }

    @Test(priority = 2)
    public void testCheckoutEmptyFirstName() {
        openCheckoutAndSubmit("", "Doe", "12345");
        Assert.assertTrue(checkoutPage.getErrorMessage().contains("First Name is required"),
                "Wrong error: " + checkoutPage.getErrorMessage());
        Assert.assertTrue(driver.getCurrentUrl().contains("checkout-step-one"), "Must stay on step one");
    }

    @Test(priority = 3)
    public void testCheckoutEmptyLastName() {
        openCheckoutAndSubmit("John", "", "12345");
        Assert.assertTrue(checkoutPage.getErrorMessage().contains("Last Name is required"),
                "Wrong error: " + checkoutPage.getErrorMessage());
        Assert.assertTrue(driver.getCurrentUrl().contains("checkout-step-one"), "Must stay on step one");
    }

    @Test(priority = 4)
    public void testCheckoutEmptyPostalCode() {
        openCheckoutAndSubmit("John", "Doe", "");
        Assert.assertTrue(checkoutPage.getErrorMessage().contains("Postal Code is required"),
                "Wrong error: " + checkoutPage.getErrorMessage());
        Assert.assertTrue(driver.getCurrentUrl().contains("checkout-step-one"), "Must stay on step one");
    }

    @Test(priority = 5)
    public void testCheckoutWithMultipleItems() {
        driver.get(BASE_URL + "inventory.html");
        ProductsPage productsPage = new ProductsPage(driver);
        productsPage.addItemToCart(1);
        productsPage.addItemToCart(2);
        Assert.assertEquals(productsPage.getCartBadgeCount(), "3");

        driver.get(BASE_URL + "cart.html");
        waitForUrl("cart.html");
        cartPage = new CartPage(driver);
        Assert.assertEquals(cartPage.getNumberOfItemsInCart(), 3);

        openCheckoutAndSubmit("Jane", "Smith", "54321");
        finishOrderAndAssertConfirmation();
    }

    @Test(priority = 6)
    public void testCancelCheckout() {
        cartPage.clickCheckout();
        waitForUrl("checkout-step-one");
        checkoutPage.clickCancel();
        waitForUrl("cart.html");
        Assert.assertTrue(driver.getCurrentUrl().contains("cart.html"), "Cancel should return to the cart");
    }

    @Test(priority = 7)
    public void testCheckoutWithSpecialCharacters() {
        openCheckoutAndSubmit("Jöhn", "D'Öe", "123-45");
        waitForUrl("checkout-step-two");
        Assert.assertTrue(driver.getCurrentUrl().contains("checkout-step-two"),
                "Special characters should be accepted on the information step");
    }
}
