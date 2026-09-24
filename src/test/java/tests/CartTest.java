package tests;

import org.openqa.selenium.By;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.sele3.asserts.Assert;
import com.sele3.elements.BaseElement;
import com.sele3.elements.Element;
import com.sele3.reports.ReportRunner;

import base.TestBase;
import listeners.TestListener;

@Listeners(TestListener.class)
public class CartTest extends TestBase {
    private final BaseElement cartLink = new Element(By.cssSelector("a[href*='/cart/']"));
    private final BaseElement emptyCartHeading = new Element(By.cssSelector(".cart-empty h1"));

    @Test(description = "An empty cart shows the 'shopping cart is empty' message")
    public void emptyCartShowsEmptyMessage() {
        ReportRunner.step("Open the cart page", () -> cartLink.click());

        Assert.expect(emptyCartHeading, "Empty cart heading is visible").toBeVisible();
        Assert.expect(emptyCartHeading, "Empty cart heading shows the expected message").toContainText("YOUR SHOPPING CART IS EMPTY");
    }
}
