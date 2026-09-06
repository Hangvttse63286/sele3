package com.sele3.elements;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import com.sele3.drivers.DriverRunner;
import com.sele3.waits.RetryAction;
import com.sele3.waits.SeleniumWait;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for page-object elements: wraps a {@link By} locator (fixed or resolved from a
 * dynamic XPath template) and exposes wait-aware accessors/actions on it, retrying via
 * {@link RetryAction} on transient Selenium exceptions rather than failing on the first flake.
 */
@Slf4j
@Data
public class BaseElement {
    /** The locator currently used to find this element; set directly or resolved via {@link #set(Object...)}. */
    protected By locator;
    /** The XPath template this element was constructed with, e.g. {@code "//div[@id='%s']"}; {@code null} for a fixed locator. */
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
     * Waits for this element to exist in the DOM, then returns it.
     *
     * @return the underlying {@link WebElement}
     */
    public WebElement getElement() {
        return SeleniumWait.waitForExist(this);
    }

    /**
     * Waits for at least one matching element to exist in the DOM, then returns them.
     *
     * @return all matching {@link WebElement}s
     */
    public List<WebElement> getElements() {
        return SeleniumWait.waitForAllExist(this);
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
        return RetryAction.retry(() -> getElement().getAttribute(attribute), RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Gets the visible (rendered) text of this element.
     *
     * @return the element's visible text
     */
    public String getText() {
        return RetryAction.retry(() -> getElement().getText(), RetryAction.COMMON_EXCEPTIONS);
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
        return RetryAction.retry(() -> getElement().getCssValue(name), RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Gets the computed value of a CSS property for every matching element.
     *
     * @param name the CSS property name
     * @return the computed CSS value for each matching element, in DOM order
     */
    public List<String> getAllCssValues(String name) {
        return RetryAction.retry(() -> getElements().stream().map(e -> e.getCssValue(name)).collect(Collectors.toList()), RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Gets the visible text of every matching element.
     *
     * @return the visible text of each matching element, in DOM order
     */
    public List<String> getAllTexts() {
        return RetryAction.retry(() -> getElements().stream().map(WebElement::getText).collect(Collectors.toList()), RetryAction.COMMON_EXCEPTIONS);
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
        RetryAction.retry(() -> {
            scrollToCenter();
            SeleniumWait.waitForClickable(this).click();
        }, RetryAction.CLICK_EXCEPTIONS);
    }

    /**
     * Scrolls the element to the center of the viewport and right-clicks it.
     */
    public void rightClick() {
        RetryAction.retry(() -> {
            scrollToCenter();
            new Actions(DriverRunner.getWebDriver()).contextClick(SeleniumWait.waitForClickable(this)).perform();
        }, RetryAction.CLICK_EXCEPTIONS);
    }

    /**
     * Scrolls the element to the center of the viewport and double-clicks it.
     */
    public void doubleClick() {
        RetryAction.retry(() -> {
            scrollToCenter();
            new Actions(DriverRunner.getWebDriver()).doubleClick(SeleniumWait.waitForClickable(this)).perform();
        }, RetryAction.CLICK_EXCEPTIONS);
    }

    /**
     * Scrolls the element to the center of the viewport and hovers the mouse over it.
     */
    public void hover() {
        RetryAction.retry(() -> {
            scrollToCenter();
            new Actions(DriverRunner.getWebDriver()).moveToElement(SeleniumWait.waitForVisible(this)).perform();
        }, RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Clears this element's current value, then types the given character sequences into it.
     *
     * @param values the character sequences to send
     */
    public void clearAndEnter(CharSequence... values) {
        RetryAction.retry(() -> {
            WebElement element = SeleniumWait.waitForClickable(this);
            element.clear();
            element.sendKeys(values);
        }, RetryAction.SEND_KEYS_EXCEPTIONS);
    }

    /**
     * Types the given character sequences into this element, without clearing its current value first.
     *
     * @param values the character sequences to send
     */
    public void enter(CharSequence... values) {
        RetryAction.retry(() -> {
            SeleniumWait.waitForClickable(this).sendKeys(values);
        }, RetryAction.SEND_KEYS_EXCEPTIONS);
    }

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its visible text.
     *
     * @param option the visible text of the option to select
     */
    public void select(String option) {
        RetryAction.retry(() -> {
            Select select = new Select(getElement());
            select.selectByVisibleText(option);
        }, RetryAction.CLICK_EXCEPTIONS);
    }

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its {@code value} attribute.
     *
     * @param value the {@code value} attribute of the option to select
     */
    public void selectByValue(String value) {
        RetryAction.retry(() -> {
            Select select = new Select(getElement());
            select.selectByValue(value);
        }, RetryAction.CLICK_EXCEPTIONS);
    }

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its index.
     *
     * @param index the zero-based index of the option to select
     */
    public void selectByIndex(int index) {
        RetryAction.retry(() -> {
            Select select = new Select(getElement());
            select.selectByIndex(index);
        }, RetryAction.CLICK_EXCEPTIONS);
    }

    /**
     * Gets the visible text of the currently selected option in this {@code <select>} element.
     *
     * @return the selected option's visible text
     */
    public String getSelectedOption() {
        return RetryAction.retry(() -> {
            Select select = new Select(getElement());
            return select.getFirstSelectedOption().getText();
        }, RetryAction.CLICK_EXCEPTIONS);
    }

    /**
     * Gets the visible text of every option in this {@code <select>} element.
     *
     * @return the visible text of each option, in DOM order
     */
    public List<String> getAllSelectedOptions() {
        return RetryAction.retry(() -> {
            Select select = new Select(getElement());
            return select.getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
        }, RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Scrolls the element to the center of the viewport and clicks it via JavaScript,
     * bypassing Selenium's native click (useful when the element is covered or not
     * otherwise clickable).
     */
    public void clickViaJS() {
        RetryAction.retry(() -> {
            scrollToCenter();
            DriverRunner.executeJS("arguments[0].click();", getElement());
        }, RetryAction.CLICK_EXCEPTIONS);
    }

    /**
     * Scrolls the element into the center of the viewport, both vertically and horizontally.
     */
    public void scrollToCenter() {
        DriverRunner.executeJS("arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});", getElement());
    }
}
