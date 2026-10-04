package tests;

import java.util.List;

import org.openqa.selenium.By;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.sele3.asserts.SoftAssert.softly;
import com.sele3.drivers.DriverRunner;
import com.sele3.elements.BaseElement;
import com.sele3.elements.Element;
import com.sele3.listeners.TestNgListener;

import base.TestBase;

/**
 * Exercises SoftAssert across String, Object, Boolean, Number, Collection and Element assertions in
 * one pass over the homepage's header: every check runs and is recorded, then TestListener's
 * automatic assertAll() reports them all together after the test, instead of stopping at
 * the first failure the way Assert would.
 */
@Listeners(TestNgListener.class)
public class HomePageTest extends TestBase {
    private final BaseElement cartLink = new Element(By.cssSelector("a[href*='/cart/']"));
    private final BaseElement contactLink = new Element(By.cssSelector("a[href*='/contact/']"));
    private final BaseElement searchInput = new Element(By.cssSelector("input[name='s']"));

    @Test(description = "Verifies homepage header content using SoftAssert across every matcher type")
    public void homePageHeaderHasExpectedContent() {

        // String
        softly.assertThat(DriverRunner.getPageTitle()).as("Home page title contains the brand name")
                .contains("NonexistentBrandName"); // INTENTIONALLY WRONG, to demo a failure
        softly.assertThat(DriverRunner.getPageTitle()).as("Home page title starts with the brand name").startsWith("TestArchitect");
        softly.assertThat(DriverRunner.getPageTitle()).as("Home page title is not empty").isNotEmpty();
        softly.assertThat(DriverRunner.getCurrentUrl()).as("Current URL after loading the home page").isEqualTo("https://demo.testarchitect.com/");
        softly.assertThat(searchInput.getAttribute("placeholder")).as("Search box placeholder text").isEqualToIgnoringCase("type here...");
        softly.assertThat(cartLink.getAttribute("target")).as("Cart link does not open in a new tab").isEmpty();

        // Object
        softly.assertThat(DriverRunner.getPageTitle()).as("Home page title is present").isNull(); // INTENTIONALLY WRONG, to demo a failure

        // Boolean
        softly.assertThat(searchInput.isEnabled()).as("Search box is enabled").isFalse(); // INTENTIONALLY WRONG, to demo a failure
        softly.assertThat(contactLink.isDisabled()).as("Contact link is not disabled").isFalse();

        // Number
        List<String> contactLinkTexts = contactLink.getAllTexts();
        softly.assertThat(contactLinkTexts.size()).as("Number of 'Contact Us' links found").isEqualTo(99); // INTENTIONALLY WRONG, to demo a failure
        softly.assertThat(contactLinkTexts.size()).as("At least one 'Contact Us' link exists").isGreaterThan(0);
        softly.assertThat(contactLinkTexts.size()).as("At least 3 'Contact Us' links exist").isGreaterThanOrEqualTo(3);
        softly.assertThat(contactLinkTexts.size()).as("'Contact Us' link count is reasonable").isLessThan(10);
        softly.assertThat(contactLinkTexts.size()).as("'Contact Us' link count is within the expected range").isBetween(1, 5);

        // Collection
        softly.assertThat(contactLinkTexts).as("'Contact Us' link texts contain the expected label")
                .contains("NonexistentLinkText"); // INTENTIONALLY WRONG, to demo a failure
        softly.assertThat(contactLinkTexts).as("'Contact Us' link texts collection has the expected size").hasSize(3);
        softly.assertThat(contactLinkTexts).as("'Contact Us' link texts collection is not empty").isNotEmpty();

        // Element
        softly.assertThat(cartLink).as("Cart link is visible").isVisible();
        softly.assertThat(cartLink).as("Cart link is attached to the DOM").isAttached();
        softly.assertThat(cartLink).as("Cart link is interactable").isInteractable();
        softly.assertThat(cartLink).as("Cart link is enabled").isEnabled();
        softly.assertThat(cartLink).as("Cart link shows the cart total").containsText("$999.99"); // INTENTIONALLY WRONG, to demo a failure
        softly.assertThat(cartLink).as("Cart link shows the cart item count").containsText("0");
        softly.assertThat(searchInput).as("Search box is visible").isVisible();
        softly.assertThat(searchInput).as("Search box has the expected placeholder attribute").hasAttribute("placeholder", "Type here...");
        softly.assertThat(searchInput).as("Search box does not have an unexpected value").doesNotHaveValue("nonexistent-value-xyz");
    }
}
