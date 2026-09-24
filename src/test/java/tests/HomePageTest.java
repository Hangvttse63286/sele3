package tests;

import java.util.List;

import org.openqa.selenium.By;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.sele3.asserts.SoftAssert;
import com.sele3.drivers.DriverRunner;
import com.sele3.elements.BaseElement;
import com.sele3.elements.Element;

import base.TestBase;
import listeners.TestListener;

/**
 * Exercises SoftAssert together with every *Expect matcher type in one pass over the homepage's
 * header: every check runs and is recorded before assertAll() reports them all together, instead
 * of stopping at the first failure the way Assert would.
 */
@Listeners(TestListener.class)
public class HomePageTest extends TestBase {
    private final BaseElement cartLink = new Element(By.cssSelector("a[href*='/cart/']"));
    private final BaseElement contactLink = new Element(By.cssSelector("a[href*='/contact/']"));
    private final BaseElement searchInput = new Element(By.cssSelector("input[name='s']"));

    @Test(description = "Verifies homepage header content using SoftAssert across every matcher type")
    public void homePageHeaderHasExpectedContent() {

        // StringExpect
        SoftAssert.expect(DriverRunner.getPageTitle(), "Home page title contains the brand name")
                .toContain("NonexistentBrandName"); // INTENTIONALLY WRONG, to demo a failure
        SoftAssert.expect(DriverRunner.getPageTitle(), "Home page title starts with the brand name").toStartWith("TestArchitect");
        SoftAssert.expect(DriverRunner.getPageTitle(), "Home page title is not empty").toBeNotEmpty();
        SoftAssert.expect(DriverRunner.getCurrentUrl(), "Current URL after loading the home page").toEqual("https://demo.testarchitect.com/");
        SoftAssert.expect(searchInput.getAttribute("placeholder"), "Search box placeholder text").toEqualIgnoringCase("type here...");
        SoftAssert.expect(cartLink.getAttribute("target"), "Cart link does not open in a new tab").toBeEmpty();

        // ObjectExpect
        SoftAssert.expect(DriverRunner.getPageTitle(), "Home page title is present").toBeNull(); // INTENTIONALLY WRONG, to demo a failure

        // BooleanExpect
        SoftAssert.expect(searchInput.isEnabled(), "Search box is enabled").toBeFalse(); // INTENTIONALLY WRONG, to demo a failure
        SoftAssert.expect(contactLink.isDisabled(), "Contact link is not disabled").toBeFalse();

        // NumberExpect
        List<String> contactLinkTexts = contactLink.getAllTexts();
        SoftAssert.expect(contactLinkTexts.size(), "Number of 'Contact Us' links found").toEqual(99); // INTENTIONALLY WRONG, to demo a failure
        SoftAssert.expect(contactLinkTexts.size(), "At least one 'Contact Us' link exists").toBeGreaterThan(0);
        SoftAssert.expect(contactLinkTexts.size(), "At least 3 'Contact Us' links exist").toBeGreaterThanOrEqual(3);
        SoftAssert.expect(contactLinkTexts.size(), "'Contact Us' link count is reasonable").toBeLessThan(10);
        SoftAssert.expect(contactLinkTexts.size(), "'Contact Us' link count is within the expected range").toBeBetweenInclusive(1, 5);

        // CollectionExpect
        SoftAssert.expect(contactLinkTexts, "'Contact Us' link texts contain the expected label")
                .toContain("NonexistentLinkText"); // INTENTIONALLY WRONG, to demo a failure
        SoftAssert.expect(contactLinkTexts, "'Contact Us' link texts collection has the expected size").toHaveSize(3);
        SoftAssert.expect(contactLinkTexts, "'Contact Us' link texts collection is not empty").toBeNotEmpty();

        // ElementExpect
        SoftAssert.expect(cartLink, "Cart link is visible").toBeVisible();
        SoftAssert.expect(cartLink, "Cart link is attached to the DOM").toBeAttached();
        SoftAssert.expect(cartLink, "Cart link is interactable").toBeInteractable();
        SoftAssert.expect(cartLink, "Cart link is enabled").toBeEnabled();
        SoftAssert.expect(cartLink, "Cart link shows the cart total").toContainText("$999.99"); // INTENTIONALLY WRONG, to demo a failure
        SoftAssert.expect(cartLink, "Cart link shows the cart item count").toContainText("0");
        SoftAssert.expect(searchInput, "Search box is visible").toBeVisible();
        SoftAssert.expect(searchInput, "Search box has the expected placeholder attribute").toHaveAttribute("placeholder", "Type here...");
        SoftAssert.expect(searchInput, "Search box does not have an unexpected value").toNotHaveValue("nonexistent-value-xyz");

        SoftAssert.assertAll();
    }
}
