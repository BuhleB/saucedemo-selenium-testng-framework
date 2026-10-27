package com.saucedemo.pages;

import com.saucedemo.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class CartPage extends BasePage {

    // Page Elements
    @FindBy(className = "title")
    private WebElement pageTitle;

    @FindBy(id = "checkout")
    private WebElement checkoutButton;

    @FindBy(id = "continue-shopping")
    private WebElement continueShoppingButton;

    @FindBy(className = "cart_item")
    private List<WebElement> cartItems;

    @FindBy(className = "cart_quantity")
    private List<WebElement> cartQuantities;

    @FindBy(className = "inventory_item_name")
    private List<WebElement> itemNames;

    @FindBy(className = "inventory_item_price")
    private List<WebElement> itemPrices;

    // Constructor
    public CartPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    // ========== PAGE VERIFICATION METHODS ==========

    public boolean isCartPageDisplayed() {
        try {
            waitForElementToBeVisible(pageTitle);
            return pageTitle.getText().equals("Your Cart") ||
                    driver.getCurrentUrl().contains("cart.html");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOnCartPage() {
        return isCartPageDisplayed();
    }

    public String getCartPageTitle() {
        try {
            waitForElementToBeVisible(pageTitle);
            return pageTitle.getText();
        } catch (Exception e) {
            return "";
        }
    }

    // ========== CART ITEM METHODS ==========

    public int getNumberOfItemsInCart() {
        try {
            return cartItems.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isCartEmpty() {
        return getNumberOfItemsInCart() == 0;
    }

    public String getItemName(int index) {
        if (index < itemNames.size()) {
            return itemNames.get(index).getText();
        }
        return "";
    }

    public String getItemPrice(int index) {
        if (index < itemPrices.size()) {
            return itemPrices.get(index).getText();
        }
        return "";
    }

    public String getItemQuantity(int index) {
        if (index < cartQuantities.size()) {
            return cartQuantities.get(index).getText();
        }
        return "";
    }

    // ========== ACTION METHODS ==========

    public void clickCheckout() {
        click(checkoutButton);
    }

    public void clickContinueShopping() {
        click(continueShoppingButton);
    }

    public void removeItem(int index) {
        if (index < cartItems.size()) {
            WebElement item = cartItems.get(index);
            WebElement removeButton = item.findElement(By.xpath(".//button[contains(text(), 'Remove')]"));
            click(removeButton);
        }
    }

    public void removeItem(String itemName) {
        for (int i = 0; i < itemNames.size(); i++) {
            if (itemNames.get(i).getText().equals(itemName)) {
                removeItem(i);
                break;
            }
        }
    }

    public void removeAllItems() {
        for (int i = cartItems.size() - 1; i >= 0; i--) {
            removeItem(i);
        }
    }

    // ========== BUTTON VERIFICATION METHODS ==========

    public boolean isCheckoutButtonDisplayed() {
        try {
            return checkoutButton.isDisplayed() && checkoutButton.isEnabled();
        } catch (Exception e) {
            return false;
        }

    }

    public void clickCheckoutButton() {
        clickCheckout(); // Call the existing method
    }
    public boolean isContinueShoppingButtonDisplayed() {
        try {
            return continueShoppingButton.isDisplayed() && continueShoppingButton.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    // ========== UTILITY METHODS ==========

    public boolean verifyCartContainsItem(String itemName) {
        for (WebElement nameElement : itemNames) {
            if (nameElement.getText().equals(itemName)) {
                return true;
            }
        }
        return false;
    }

    public double getTotalPrice() {
        double total = 0.0;
        for (WebElement priceElement : itemPrices) {
            String priceText = priceElement.getText().replace("$", "");
            total += Double.parseDouble(priceText);
        }
        return total;
    }
}