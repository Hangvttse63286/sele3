package tests;

import org.openqa.selenium.By;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.sele3.asserts.Assert;
import com.sele3.elements.BaseElement;
import com.sele3.elements.Element;
import com.sele3.listeners.TestNgListener;
import com.sele3.reports.ReportRunner;

import base.TestBase;

@Listeners(TestNgListener.class)
public class CartTest extends TestBase {
    private final BaseElement cartLink = new Element(By.cssSelector("a[href*='/cart/']"));
    private final BaseElement emptyCartHeading = new Element(By.cssSelector(".cart-empty h1"));

    @Test(description = "An empty cart shows the 'shopping cart is empty' message")
    public void emptyCartShowsEmptyMessage() {
        ReportRunner.step("Open the cart page", () -> cartLink.click());

        Assert.assertThat(emptyCartHeading).as("Empty cart heading is visible").isVisible();
        Assert.assertThat(emptyCartHeading).as("Empty cart heading shows the expected message").containsText("YOUR SHOPPING CART IS EMPTY");
    }
}
