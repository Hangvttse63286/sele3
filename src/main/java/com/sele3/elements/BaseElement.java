package com.sele3.elements;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import com.sele3.drivers.DriverRunner;
import com.sele3.waits.SeleniumWait;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class BaseElement {
    protected By locator;
    protected String dynamicXPathLocator;

    /**
     * Creates a BaseElement from a fixed {@link By} locator.
     *
     * @param locator the locator used to find this element
     */
    public BaseElement(By locator) {
        this.locator = locator;
    }

    /**
     * Creates a BaseElement from a dynamic XPath template, to be resolved later via {@link #set(Object...)}.
     *
     * @param dynamicXPathLocator an XPath template, e.g. {@code "//div[@id='%s']"}
     */
    public BaseElement(String dynamicXPathLocator) {
        this.dynamicXPathLocator = dynamicXPathLocator;
    }

    /**
     * Waits for this element to be visible, then returns it.
     *
     * @return the underlying {@link WebElement}
     */
    public WebElement getElement() {
        waitForVisible();
        return waitForExist();
    }

    /**
     * Waits for all matching elements to be visible, then returns them.
     *
     * @return all matching {@link WebElement}s
     */
    public List<WebElement> getElements() {
        waitForAllVisible();
        return waitForAllExist();
    }

    /**
     * Resolves this element's dynamic XPath template by substituting the given arguments,
     * replacing the current locator.
     *
     * @param args the values to substitute into the XPath template
     */
    public void set(Object... args) {
        this.locator = By.xpath(String.format(this.dynamicXPathLocator, args));
    }

    /**
     * Gets the value of the given attribute or property.
     *
     * @param attribute the attribute/property name
     * @return the attribute's value, or {@code null} if not present
     */
    public String getAttribute(String attribute) {
        return getElement().getAttribute(attribute);
    }

    /**
     * Gets the visible (rendered) text of this element.
     *
     * @return the element's visible text
     */
    public String getText() {
        return getElement().getText();
    }

    /**
     * Gets this element's {@code innerText} property.
     *
     * @return the element's {@code innerText}
     */
    public String getInnerText() {
        return getAttribute("innerText");
    }

    /**
     * Gets this element's {@code value} attribute.
     *
     * @return the element's {@code value}
     */
    public String getValue() {
        return getAttribute("value");
    }

    /**
     * Gets the computed value of a CSS property on this element.
     *
     * @param name the CSS property name
     * @return the computed CSS value
     */
    public String getCssValue(String name) {
        return getElement().getCssValue(name);
    }

    /**
     * Gets the computed value of a CSS property for every matching element.
     *
     * @param name the CSS property name
     * @return the computed CSS value for each matching element, in DOM order
     */
    public List<String> getAllCssValues(String name) {
        return getElements().stream().map(e -> e.getCssValue(name)).collect(Collectors.toList());
    }

    /**
     * Gets the visible text of every matching element.
     *
     * @return the visible text of each matching element, in DOM order
     */
    public List<String> getAllTexts() {
        return getElements().stream().map(WebElement::getText).collect(Collectors.toList());
    }

    /**
     * Scrolls the element into view, aligning it to the top of the viewport.
     */
    public void scrollToView() {
        DriverRunner.executeJS("arguments[0].scrollIntoView(true);", getElement());
    }

    /**
     * Scrolls the element to the center of the viewport and left-clicks it.
     */
    public void click() {
        scrollToCenter();
        waitForClickable().click();
    }

    /**
     * Scrolls the element to the center of the viewport and right-clicks it.
     */
    public void rightClick() {
        scrollToCenter();
        new Actions(DriverRunner.getWebDriver()).contextClick(waitForClickable()).perform();
    }

    /**
     * Scrolls the element to the center of the viewport and double-clicks it.
     */
    public void doubleClick() {
        scrollToCenter();
        new Actions(DriverRunner.getWebDriver()).doubleClick(waitForClickable()).perform();
    }

    /**
     * Scrolls the element to the center of the viewport and hovers the mouse over it.
     */
    public void hover() {
        scrollToCenter();
        new Actions(DriverRunner.getWebDriver()).moveToElement(getElement()).perform();
    }

    /**
     * Checks whether this element is selected (e.g. a checkbox, radio button, or option).
     *
     * @return {@code true} if selected
     */
    public boolean isSelected() {
        return getElement().isSelected();
    }

    /**
     * Checks whether this element is enabled.
     *
     * @return {@code true} if enabled
     */
    public boolean isEnabled() {
        return getElement().isEnabled();
    }

    /**
     * Checks whether this element is checked. Alias for {@link #isSelected()}.
     *
     * @return {@code true} if checked
     */
    public boolean isChecked() {
        return isSelected();
    }

    /**
     * Clicks this element only if it is not already selected/checked.
     */
    public void check() {
        if (!isSelected()) {
            click();
        }
    }

    /**
     * Clicks this element only if it is currently selected/checked.
     */
    public void uncheck() {
        if (isSelected()) {
            click();
        }
    }

    /**
     * Sends the given character sequences to this element (e.g. typing into an input).
     *
     * @param values the character sequences to send
     */
    public void enterText(CharSequence... values) {
        getElement().sendKeys(values);
    }

    /**
     * Clears this element's current value, then types the given character sequences into it.
     *
     * @param values the character sequences to send
     */
    public void clearAndEnterText(CharSequence... values) {
        getElement().clear();
        this.enterText(values);
    }

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its visible text.
     *
     * @param option the visible text of the option to select
     */
    public void select(String option) {
        Select select = new Select(getElement());
        select.selectByVisibleText(option);
    }

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its {@code value} attribute.
     *
     * @param value the {@code value} attribute of the option to select
     */
    public void selectByValue(String value) {
        Select select = new Select(getElement());
        select.selectByValue(value);
    }

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its index.
     *
     * @param index the zero-based index of the option to select
     */
    public void selectByIndex(int index) {
        Select select = new Select(getElement());
        select.selectByIndex(index);
    }

    /**
     * Gets the visible text of the currently selected option in this {@code <select>} element.
     *
     * @return the selected option's visible text
     */
    public String getSelectedOption() {
        Select select = new Select(getElement());
        return select.getFirstSelectedOption().getText();
    }

    /**
     * Gets the visible text of every option in this {@code <select>} element.
     *
     * @return the visible text of each option, in DOM order
     */
    public List<String> getAllSelectedOptions() {
        Select select = new Select(getElement());
        return select.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
    }

    /**
     * Scrolls the element to the center of the viewport and clicks it via JavaScript,
     * bypassing Selenium's native click (useful when the element is covered or not
     * otherwise clickable).
     */
    public void clickViaJS() {
        scrollToCenter();
        DriverRunner.executeJS("arguments[0].click();", getElement());
    }

    /**
     * Scrolls the element into the center of the viewport, both vertically and horizontally.
     */
    public void scrollToCenter() {
        DriverRunner.executeJS("arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});", getElement());
    }

    /**
     * Waits until this element is present in the DOM.
     *
     * @return the found {@link WebElement}
     */
    public WebElement waitForExist() {
        return SeleniumWait.waitForExist(getLocator());
    }

    /**
     * Waits until at least one matching element is present in the DOM.
     *
     * @return all matching {@link WebElement}s
     */
    public List<WebElement> waitForAllExist() {
        return SeleniumWait.waitForAllExist(getLocator());
    }

    /**
     * Waits until this element is present and visible.
     */
    public void waitForVisible() {
        SeleniumWait.waitForVisible(getLocator());
    }

    /**
     * Waits until all matching elements are present and visible.
     */
    public void waitForAllVisible() {
        SeleniumWait.waitForAllVisible(getLocator());
    }

    /**
     * Waits until this element is no longer visible (or no longer present).
     */
    public void waitForInvisible() {
        SeleniumWait.waitForInvisible(getLocator());
    }

    /**
     * Waits until this element is enabled.
     */
    public void waitForEnabled() {
        SeleniumWait.waitForEnabled(getElement());
    }

    /**
     * Waits until this element is disabled.
     */
    public void waitForDisabled() {
        SeleniumWait.waitForDisabled(getElement());
    }

    /**
     * Waits until this element is visible and enabled.
     *
     * @return the clickable {@link WebElement}
     */
    public WebElement waitForClickable() {
        return SeleniumWait.waitForClickable(getLocator());
    }

    /**
     * Waits until this element's {@code value} attribute equals the given value.
     *
     * @param value the expected value
     */
    public void waitForValueEquals(String value) {
        SeleniumWait.waitForValueEquals(getElement(), value);
    }

    /**
     * Waits until this element's {@code value} attribute no longer equals the given value.
     *
     * @param value the value expected to no longer match
     */
    public void waitForValueNotEquals(String value) {
        SeleniumWait.waitForValueNotEquals(getElement(), value);
    }

    /**
     * Waits until this element's {@code value} attribute contains the given text.
     *
     * @param value the substring expected to appear in the value
     */
    public void waitForValueContains(String value) {
        SeleniumWait.waitForValueContains(getElement(), value);
    }

    /**
     * Waits until this element's visible text equals the given text.
     *
     * @param text the expected text
     */
    public void waitForTextEquals(String text) {
        SeleniumWait.waitForTextEquals(getElement(), text);
    }

    /**
     * Waits until this element's visible text no longer equals the given text.
     *
     * @param text the text expected to no longer match
     */
    public void waitForTextNotEquals(String text) {
        SeleniumWait.waitForTextNotEquals(getElement(), text);
    }

    /**
     * Waits until this element contains the given text.
     *
     * @param text the substring expected to appear in the element's text
     */
    public void waitForTextContains(String text) {
        SeleniumWait.waitForTextContains(getLocator(), text);
    }

    /**
     * Waits until the given attribute on this element equals the given value.
     *
     * @param attribute the attribute name
     * @param value the expected value
     */
    public void waitForAttributeEquals(String attribute, String value) {
        SeleniumWait.waitForAttributeEquals(getElement(), attribute, value);
    }

    /**
     * Waits until the given attribute on this element no longer equals the given value.
     *
     * @param attribute the attribute name
     * @param value the value expected to no longer match
     */
    public void waitForAttributeNotEquals(String attribute, String value) {
        SeleniumWait.waitForAttributeNotEquals(getElement(), attribute, value);
    }

    /**
     * Waits until the given attribute on this element contains the given value.
     *
     * @param attribute the attribute name
     * @param value the substring expected to appear in the attribute's value
     */
    public void waitForAttributeContains(String attribute, String value) {
        SeleniumWait.waitForAttributeContains(getElement(), attribute, value);
    }

    /**
     * Waits until this {@code <select>} element has its {@code <option>} children populated.
     */
    public void waitForSelectOptionsLoaded() {
        SeleniumWait.waitForSelectOptionsLoaded(getLocator());
    }

    /**
     * Waits until this element is selected/checked.
     */
    public void waitForChecked() {
        SeleniumWait.waitForChecked(getElement());
    }

    /**
     * Waits until this element is deselected/unchecked.
     */
    public void waitForUnchecked() {
        SeleniumWait.waitForUnchecked(getElement());
    }

    /**
     * Checks, without throwing, whether the given attribute on this element equals the given value.
     *
     * @param attribute the attribute name
     * @param value the expected value
     * @return {@code true} if the attribute equals the value within the wait timeout, {@code false} otherwise
     */
    public boolean attributeEquals(String attribute, String value) {
        try {
            waitForAttributeEquals(attribute, value);
            return true;
        } catch (NoSuchElementException | TimeoutException e) {
            log.debug(e.getMessage());
            return false;
        }
    }

    /**
     * Checks, without throwing, whether the given attribute on this element contains the given value.
     *
     * @param attribute the attribute name
     * @param value the substring expected to appear in the attribute's value
     * @return {@code true} if the attribute contains the value within the wait timeout, {@code false} otherwise
     */
    public boolean attributeContains(String attribute, String value) {
        try {
            waitForAttributeContains(attribute, value);
            return true;
        } catch (NoSuchElementException | TimeoutException e) {
            log.debug(e.getMessage());
            return false;
        }
    }

    /**
     * Checks, without throwing, whether this element becomes visible.
     *
     * @return {@code true} if the element is visible within the wait timeout, {@code false} otherwise
     */
    public boolean beDisplayed() {
        try {
            waitForVisible();
            return true;
        } catch (NoSuchElementException | TimeoutException e) {
            log.debug(e.getMessage());
            return false;
        }
    }

    /**
     * Waits until this element becomes stale (detached from the DOM).
     */
    public void waitForStaleness() {
        SeleniumWait.waitForStaleness(getElement());
    }

    /**
     * Checks, without throwing, whether this element's visible text equals the given text.
     *
     * @param text the expected text
     * @return {@code true} if the text matches within the wait timeout, {@code false} otherwise
     */
    public boolean textEquals(String text) {
        try {
            waitForTextEquals(text);
            return true;
        } catch (NoSuchElementException | TimeoutException e) {
            log.debug(e.getMessage());
            return false;
        }
    }

    /**
     * Checks, without throwing, whether this element exists in the DOM.
     *
     * @return {@code true} if the element is found within the wait timeout, {@code false} otherwise
     */
    public boolean exists() {
        try {
            return waitForExist() != null;
        } catch (NoSuchElementException | TimeoutException e) {
            log.debug(e.getMessage());
            return false;
        }
    }
}
