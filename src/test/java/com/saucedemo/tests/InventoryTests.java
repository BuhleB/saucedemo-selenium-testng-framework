package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.pages.LoginPage;
import com.saucedemo.pages.ProductsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InventoryTests extends BaseTest {

    private ProductsPage productsPage;

    @BeforeMethod
    public void loginBeforeTest() {
        productsPage = loginAs("standard_user", "secret_sauce");
    }

    @Test(priority = 1)
    public void testInventoryItemsCount() {
        Assert.assertEquals(productsPage.getInventoryItemCount(), 6);
    }

    @Test(priority = 2)
    public void testAddItemToCart() {
        productsPage.addItemToCart(0);
        Assert.assertEquals(productsPage.getCartBadgeCount(), "1");
    }

    @Test(priority = 3)
    public void testAddRemoveFromCart() {
        productsPage.addItemToCart(0);
        Assert.assertEquals(productsPage.getCartBadgeCount(), "1");

        productsPage.removeItemFromCart(0);
        waitForCondition(d -> productsPage.getCartCount() == 0, 20, "cart badge to disappear");
        Assert.assertEquals(productsPage.getCartCount(), 0, "Cart badge should disappear after removing the only item");
    }

    @Test(priority = 4)
    public void testLogout() {
        productsPage.logout();
        waitForCondition(d -> new LoginPage(d).isLoginPageDisplayed(), 10, "login page after logout");
        Assert.assertFalse(driver.getCurrentUrl().contains("inventory"), "Still on the inventory page after logout");
    }

    @Test(priority = 5)
    public void testProductDetailsDisplay() {
        List<String> names = productsPage.getProductNames();
        List<String> prices = productsPage.getProductPriceTexts();
        List<String> descriptions = productsPage.getProductDescriptions();

        Assert.assertEquals(names.size(), 6);
        Assert.assertEquals(prices.size(), 6);
        Assert.assertEquals(descriptions.size(), 6);
        for (int i = 0; i < names.size(); i++) {
            Assert.assertFalse(names.get(i).isEmpty(), "Product " + (i + 1) + " has no name");
            Assert.assertTrue(prices.get(i).startsWith("$"), "Product " + (i + 1) + " has a bad price: " + prices.get(i));
            Assert.assertFalse(descriptions.get(i).isEmpty(), "Product " + (i + 1) + " has no description");
        }
    }

    @Test(priority = 6)
    public void testAddMultipleDifferentItems() {
        productsPage.addItemToCart(0);
        productsPage.addItemToCart(2);
        productsPage.addItemToCart(4);
        Assert.assertEquals(productsPage.getCartBadgeCount(), "3");
    }

    @Test(priority = 7)
    public void testCartPersistsAfterRefresh() {
        productsPage.addItemToCart(0);
        Assert.assertEquals(productsPage.getCartBadgeCount(), "1");

        driver.navigate().refresh();
        Assert.assertEquals(new ProductsPage(driver).getCartBadgeCount(), "1", "Cart should survive a page refresh");
    }

    @Test(priority = 8)
    public void testSortByPriceLowToHigh() {
        productsPage.sortBy("lohi");
        List<Double> actual = productsPage.getProductPrices();
        List<Double> expected = new ArrayList<>(actual);
        Collections.sort(expected);
        Assert.assertEquals(actual, expected, "Prices are not sorted low to high");
    }

    @Test(priority = 9)
    public void testSortByNameZToA() {
        productsPage.sortBy("za");
        List<String> actual = productsPage.getProductNames();
        List<String> expected = new ArrayList<>(actual);
        expected.sort(Comparator.reverseOrder());
        Assert.assertEquals(actual, expected, "Names are not sorted Z to A");
    }
}
