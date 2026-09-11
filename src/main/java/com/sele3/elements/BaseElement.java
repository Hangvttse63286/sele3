package com.sele3.elements;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import com.sele3.drivers.DriverRunner;
import com.sele3.waits.ElementWait;
import com.sele3.waits.RetryAction;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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
     * Lazily-created by {@link #waits()} and cached here; excluded from
     * {@code equals}/{@code hashCode}/{@code toString} as an internal cache rather than part of
     * this element's identity, and to avoid ever depending on {@link ElementWait} gaining its own
     * generated {@code equals}/{@code hashCode}/{@code toString} again — it holds a back-reference
     * to this element ({@link ElementWait#getElement()}), which would recurse infinitely if both
     * sides generated those methods.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    protected ElementWait wait;

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
     * returns the underlying {@link WebElement} without waiting for it to exist in the DOM.
     *
     * @return the underlying {@link WebElement}
     */
    protected WebElement findElement() {
        return ExpectedConditions.presenceOfElementLocated(this.locator).apply(DriverRunner.getWebDriver());
    }

    /**
     * returns the underlying {@link WebElement} without waiting for it to exist in the DOM and be interactable.
     *
     * @return the underlying {@link WebElement}
     */
    protected WebElement findInteractableElement() {
        return ExpectedConditions.elementToBeClickable(this.locator).apply(DriverRunner.getWebDriver());
    }

    /**
     * returns all matching {@link WebElement}s without waiting for them to exist in the DOM.
     *
     * @return all matching {@link WebElement}s
     */
    protected List<WebElement> findElements() {
        return ExpectedConditions.presenceOfAllElementsLocatedBy(this.locator).apply(DriverRunner.getWebDriver());
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
        return RetryAction.retry(() -> RetryAction.readyCheck(findElement()).getAttribute(attribute), RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Gets the visible (rendered) text of this element.
     *
     * @return the element's visible text
     */
    public String getText() {
        return RetryAction.retry(() -> RetryAction.readyCheck(findElement()).getText(), RetryAction.COMMON_EXCEPTIONS);
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
        return RetryAction.retry(() -> RetryAction.readyCheck(findElement()).getCssValue(name), RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Gets the computed value of a CSS property for every matching element.
     *
     * @param name the CSS property name
     * @return the computed CSS value for each matching element, in DOM order
     */
    public List<String> getAllCssValues(String name) {
        return RetryAction.retry(() -> RetryAction.readyCheck(findElements()).stream().map(e -> RetryAction.readyCheck(e).getCssValue(name)).collect(Collectors.toList()), RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Gets the visible text of every matching element.
     *
     * @return the visible text of each matching element, in DOM order
     */
    public List<String> getAllTexts() {
        return RetryAction.retry(() -> RetryAction.readyCheck(findElements()).stream().map(e -> RetryAction.readyCheck(e).getText()).collect(Collectors.toList()), RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Scrolls the element into view, aligning it to the top of the viewport.
     */
    public void scrollToView() {
        RetryAction.retry(() -> {
            scrollToView(RetryAction.readyCheck(findElement()));
        }, RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Scrolls the element into view, aligning it to the top of the viewport.
     */
    protected void scrollToView(WebElement element) {
        DriverRunner.executeJS("arguments[0].scrollIntoView(true);", element);
        
    }

    /**
     * Scrolls the element to the center of the viewport and left-clicks it.
     */
    public void click() {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            scrollToCenter(element);
            element.click();
        }, RetryAction.CLICK_EXCEPTIONS);
    }

    /**
     * Scrolls the element to the center of the viewport and right-clicks it.
     */
    public void rightClick() {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            scrollToCenter(element);
            new Actions(DriverRunner.getWebDriver()).contextClick(element).perform();
        }, RetryAction.CLICK_EXCEPTIONS);
    }

    /**
     * Scrolls the element to the center of the viewport and double-clicks it.
     */
    public void doubleClick() {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            scrollToCenter(element);
            new Actions(DriverRunner.getWebDriver()).doubleClick(element).perform();
        }, RetryAction.CLICK_EXCEPTIONS);
    }

    /**
     * Scrolls the element to the center of the viewport and hovers the mouse over it.
     */
    public void hover() {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findElement());
            scrollToCenter(element);
            new Actions(DriverRunner.getWebDriver()).moveToElement(element).perform();
        }, RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Clears this element's current value, then types the given character sequences into it.
     *
     * @param values the character sequences to send
     */
    public void clearAndEnter(CharSequence... values) {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            scrollToCenter(element);
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
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            scrollToCenter(element);
            element.sendKeys(values);
        }, RetryAction.SEND_KEYS_EXCEPTIONS);
    }

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its visible text.
     *
     * @param option the visible text of the option to select
     */
    public void select(String option) {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            Select select = new Select(element);
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
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            Select select = new Select(element);
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
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            Select select = new Select(element);
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
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            Select select = new Select(element);
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
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            Select select = new Select(element);
            return select.getOptions().stream().map(e -> RetryAction.readyCheck(e).getText()).collect(Collectors.toList());
        }, RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Scrolls the element to the center of the viewport and clicks it via JavaScript,
     * bypassing Selenium's native click (useful when the element is covered or not
     * otherwise clickable).
     */
    public void clickViaJS() {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            scrollToCenter(element);
            DriverRunner.executeJS("arguments[0].click();", element);
        }, RetryAction.CLICK_EXCEPTIONS);
    }

    /**
     * Scrolls the element into the center of the viewport, both vertically and horizontally.
     */
    public void scrollToCenter() {
        RetryAction.retry(() -> {
            scrollToCenter(RetryAction.readyCheck(findElement()));
        }, RetryAction.COMMON_EXCEPTIONS);
    }

    /**
     * Scrolls the element into the center of the viewport, both vertically and horizontally.
     */
    protected void scrollToCenter(WebElement element) {
        DriverRunner.executeJS("arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});", element);
    }

    /**
     * Returns this element's {@link ElementWait}, creating it on first use.
     *
     * @return this element's {@link ElementWait}
     */
    public ElementWait waits() {
        if (this.wait == null) {
            this.wait = new ElementWait(this);
        }
        return this.wait;
    }

    /**
     * Checks whether this element is currently enabled.
     *
     * @return {@code true} if the element is enabled
     */
    public boolean isEnabled() {
        return RetryAction.retry(() -> findElement().isEnabled(), RetryAction.COMMON_EXCEPTIONS);
    }

     /**
     * Checks whether this element is currently disabled.
     *
     * @return {@code true} if the element is disabled
     */
    public boolean isDisabled() {
        return RetryAction.retry(() -> !findElement().isEnabled(), RetryAction.COMMON_EXCEPTIONS);
    }
}
