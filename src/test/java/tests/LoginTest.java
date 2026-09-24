package tests;

import org.openqa.selenium.By;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.sele3.asserts.Assert;
import com.sele3.drivers.DriverRunner;
import com.sele3.elements.BaseElement;
import com.sele3.elements.Element;
import com.sele3.reports.ReportRunner;

import base.TestBase;
import listeners.TestListener;

@Listeners(TestListener.class)
public class LoginTest extends TestBase {
    private final BaseElement accountLink = new Element(By.cssSelector("a[href*='/my-account/']"));
    private final BaseElement username = new Element(By.id("username"));
    private final BaseElement password = new Element(By.id("password"));
    private final BaseElement loginButton = new Element(By.cssSelector("button[name='login']"));
    private final BaseElement errorNotice = new Element(By.cssSelector(".woocommerce-error"));

    @Test(description = "Logging in with an unregistered username shows a WooCommerce error notice")
    public void loginWithUnknownUsernameFails() {
        ReportRunner.step("Open the My Account page", () -> accountLink.click());
        Assert.expect(DriverRunner.getCurrentUrl(), "Current URL is the My Account page").toContain("/my-account/");

        ReportRunner.step("Enter username", () -> username.clearAndEnter("nonexistent_user_12345"));
        ReportRunner.step("Enter password", () -> password.clearAndEnter("wrongpass"));
        ReportRunner.step("Click login button", () -> loginButton.click());

        Assert.expect(errorNotice, "WooCommerce error notice is visible").toBeVisible();
        Assert.expect(errorNotice, "Error notice shows the unregistered-username message").toContainText("is not registered on this site");
    }
}
