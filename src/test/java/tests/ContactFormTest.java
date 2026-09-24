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
public class ContactFormTest extends TestBase {
    private final BaseElement contactLink = new Element(By.cssSelector("a[href*='/contact/']"));
    private final BaseElement nameField = new Element(By.cssSelector("input[placeholder='Your Name (required)']"));
    private final BaseElement sendButton = new Element(By.cssSelector("input[type='submit'][value='Send message']"));

    @Test(description = "Submitting the contact form with required fields empty marks them invalid")
    public void submittingEmptyRequiredFieldsMarksThemInvalid() {
        ReportRunner.step("Open the contact page", () -> contactLink.click());
        ReportRunner.step("Scroll the contact form into view", () -> nameField.scrollToView());

        Assert.expect(nameField, "Name field is not marked invalid before submitting").toHaveAttribute("aria-invalid", "false");

        ReportRunner.step("Submit the contact form", () -> sendButton.click());

        Assert.expect(nameField, "Name field is marked invalid after submitting empty").toHaveAttribute("aria-invalid", "true");
    }
}
