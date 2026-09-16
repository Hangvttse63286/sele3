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
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for page-object elements: wraps a {@link By} locator (fixed or resolved from a
 * dynamic XPath template) and exposes wait-aware accessors/actions on it, retrying via
 * {@link RetryAction} on transient Selenium exceptions rather than failing on the first
 * transient failure. The current-thread WebDriver must be initialized before invoking methods
 * that access the browser, including {@link #waits()}.
 */
@Slf4j
@Data
public class Element implements BaseElement {
    /** The locator currently used to find this element; resolved via {@link #set(Object...)} for dynamic elements. */
    protected By locator;
    /** The XPath template used by dynamic elements, e.g. {@code "//div[@id='%s']"}; {@code null} for fixed locators. */
    protected String dynamicXPathLocator;

    /**
    * Creates a {@code Element} from a fixed {@link By} locator.
     *
     * @param locator the locator used to find this element
     */
    public Element(By locator) {
        this.locator = locator;
    }

    /**
    * Creates a {@code Element} from a dynamic XPath template. Call {@link #set(Object...)}
    * before using this element to resolve the template into a locator.
     *
     * @param dynamicXPathLocator an XPath template, e.g. {@code "//div[@id='%s']"}
     */
    public Element(String dynamicXPathLocator) {
        this.dynamicXPathLocator = dynamicXPathLocator;
    }

    /**
    * Returns the first matching {@link WebElement} if it is present in the DOM.
    * This method performs a single presence check and does not poll until the element appears.
     *
     * @return the underlying {@link WebElement}
     */
    protected WebElement findElement() {
        return ExpectedConditions.presenceOfElementLocated(this.locator).apply(DriverRunner.getWebDriver());
    }

    /**
    * Returns the first matching {@link WebElement} if it is present and clickable.
    * This method performs a single clickability check and does not poll until the element becomes clickable.
     *
     * @return the underlying {@link WebElement}
     */
    protected WebElement findInteractableElement() {
        return ExpectedConditions.elementToBeClickable(this.locator).apply(DriverRunner.getWebDriver());
    }

    /**
    * Returns all matching {@link WebElement}s if they are present in the DOM.
    * This method performs a single presence check and does not poll until the elements appear.
     *
     * @return all matching {@link WebElement}s
     */
    protected List<WebElement> findElements() {
        return ExpectedConditions.presenceOfAllElementsLocatedBy(this.locator).apply(DriverRunner.getWebDriver());
    }

    /**
    * Resolves this element's dynamic XPath template by substituting the given arguments and
    * replacing the current locator. This method is intended for elements created with
    * {@link #Element(String)}.
     *
     * @param args the values to substitute into the XPath template
     */
    @Override
    public void set(Object... args) {
        this.locator = By.xpath(String.format(this.dynamicXPathLocator, args));
    }

    /**
     * Gets the value of the given attribute or property.
     *
     * @param attribute the attribute/property name
     * @return the attribute's value, or {@code null} if not present
     */
    @Override
    public String getAttribute(String attribute) {
        return RetryAction.retry(() -> RetryAction.readyCheck(findElement()).getAttribute(attribute), RetryAction.COMMON_EXCEPTIONS,
            () -> "get attribute '" + attribute + "' of element " + locator);
    }

    /**
     * Gets the visible (rendered) text of this element.
     *
     * @return the element's visible text
     */
    @Override
    public String getText() {
        return RetryAction.retry(() -> RetryAction.readyCheck(findElement()).getText(), RetryAction.COMMON_EXCEPTIONS,
            () -> "get text of element " + locator);
    }

    /**
     * Gets this element's {@code innerText} property.
     *
     * @return the element's {@code innerText}
     */
    @Override
    public String getInnerText() {
        return getAttribute("innerText");
    }

    /**
     * Gets this element's {@code value} attribute.
     *
     * @return the element's {@code value}
     */
    @Override
    public String getValue() {
        return getAttribute("value");
    }

    /**
     * Gets the computed value of a CSS property on this element.
     *
     * @param name the CSS property name
     * @return the computed CSS value
     */
    @Override
    public String getCssValue(String name) {
        return RetryAction.retry(() -> RetryAction.readyCheck(findElement()).getCssValue(name), RetryAction.COMMON_EXCEPTIONS,
            () -> "get CSS value '" + name + "' of element " + locator);
    }

    /**
     * Gets the computed value of a CSS property for every matching element.
     *
     * @param name the CSS property name
     * @return the computed CSS value for each matching element, in DOM order
     */
    @Override
    public List<String> getAllCssValues(String name) {
        return RetryAction.retry(() -> RetryAction.readyCheck(findElements()).stream().map(e -> e.getCssValue(name)).collect(Collectors.toList()), RetryAction.COMMON_EXCEPTIONS,
            () -> "get CSS value '" + name + "' of all elements " + locator);
    }

    /**
     * Gets the visible text of every matching element.
     *
     * @return the visible text of each matching element, in DOM order
     */
    @Override
    public List<String> getAllTexts() {
        return RetryAction.retry(() -> RetryAction.readyCheck(findElements()).stream().map(e -> e.getText()).collect(Collectors.toList()), RetryAction.COMMON_EXCEPTIONS,
            () -> "get text of all elements " + locator);
    }

    /**
     * Scrolls the element into view, aligning it to the top of the viewport.
     */
    @Override
    public void scrollToView() {
        RetryAction.retry(() -> {
            scrollToView(RetryAction.readyCheck(findElement()));
        }, RetryAction.COMMON_EXCEPTIONS, () -> "scroll element " + locator + " into view");
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
    @Override
    public void click() {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            scrollToCenter(element);
            element.click();
        }, RetryAction.CLICK_EXCEPTIONS, () -> "click element " + locator);
    }

    /**
     * Scrolls the element to the center of the viewport and right-clicks it.
     */
    @Override
    public void rightClick() {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            scrollToCenter(element);
            new Actions(DriverRunner.getWebDriver()).contextClick(element).perform();
        }, RetryAction.CLICK_EXCEPTIONS, () -> "right-click element " + locator);
    }

    /**
     * Scrolls the element to the center of the viewport and double-clicks it.
     */
    @Override
    public void doubleClick() {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            scrollToCenter(element);
            new Actions(DriverRunner.getWebDriver()).doubleClick(element).perform();
        }, RetryAction.CLICK_EXCEPTIONS, () -> "double-click element " + locator);
    }

    /**
     * Scrolls the element to the center of the viewport and hovers the mouse over it.
     */
    @Override
    public void hover() {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findElement());
            scrollToCenter(element);
            new Actions(DriverRunner.getWebDriver()).moveToElement(element).perform();
        }, RetryAction.COMMON_EXCEPTIONS, () -> "hover over element " + locator);
    }

    /**
     * Clears this element's current value, then types the given character sequences into it.
     *
     * @param values the character sequences to send
     */
    @Override
    public void clearAndEnter(CharSequence... values) {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            scrollToCenter(element);
            element.clear();
            element.sendKeys(values);
        }, RetryAction.SEND_KEYS_EXCEPTIONS, () -> "clear and enter text into element " + locator);
    }

    /**
     * Types the given character sequences into this element, without clearing its current value first.
     *
     * @param values the character sequences to send
     */
    @Override
    public void enter(CharSequence... values) {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            scrollToCenter(element);
            element.sendKeys(values);
        }, RetryAction.SEND_KEYS_EXCEPTIONS, () -> "enter text into element " + locator);
    }

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its visible text.
     *
     * @param option the visible text of the option to select
     */
    @Override
    public void select(String option) {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            Select select = new Select(element);
            select.selectByVisibleText(option);
        }, RetryAction.CLICK_EXCEPTIONS, () -> "select option '" + option + "' in element " + locator);
    }

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its {@code value} attribute.
     *
     * @param value the {@code value} attribute of the option to select
     */
    @Override
    public void selectByValue(String value) {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            Select select = new Select(element);
            select.selectByValue(value);
        }, RetryAction.CLICK_EXCEPTIONS, () -> "select option with value '" + value + "' in element " + locator);
    }

    /**
     * Selects an {@code <option>} in this {@code <select>} element by its index.
     *
     * @param index the zero-based index of the option to select
     */
    @Override
    public void selectByIndex(int index) {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            Select select = new Select(element);
            select.selectByIndex(index);
        }, RetryAction.CLICK_EXCEPTIONS, () -> "select option at index " + index + " in element " + locator);
    }

    /**
     * Gets the visible text of the currently selected option in this {@code <select>} element.
     *
     * @return the selected option's visible text
     */
    @Override
    public String getSelectedOption() {
        return RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            Select select = new Select(element);
            return select.getFirstSelectedOption().getText();
        }, RetryAction.CLICK_EXCEPTIONS, () -> "get selected option of element " + locator);
    }

    /**
     * Gets the visible text of every option in this {@code <select>} element.
     *
     * @return the visible text of each option, in DOM order
     */
    @Override
    public List<String> getAllSelectedOptions() {
        return RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            Select select = new Select(element);
            return select.getOptions().stream().map(e -> e.getText()).collect(Collectors.toList());
        }, RetryAction.COMMON_EXCEPTIONS, () -> "get all options of element " + locator);
    }

    /**
     * Scrolls the element to the center of the viewport and clicks it via JavaScript,
     * bypassing Selenium's native click (useful when the element is covered or not
     * otherwise clickable).
     */
    @Override
    public void clickViaJS() {
        RetryAction.retry(() -> {
            WebElement element = RetryAction.readyCheck(findInteractableElement());
            scrollToCenter(element);
            DriverRunner.executeJS("arguments[0].click();", element);
        }, RetryAction.CLICK_EXCEPTIONS, () -> "click element " + locator + " via JavaScript");
    }

    /**
     * Scrolls the element into the center of the viewport, both vertically and horizontally.
     */
    @Override
    public void scrollToCenter() {
        RetryAction.retry(() -> {
            scrollToCenter(RetryAction.readyCheck(findElement()));
        }, RetryAction.COMMON_EXCEPTIONS, () -> "scroll element " + locator + " into center of viewport");
    }

    /**
     * Scrolls the element into the center of the viewport, both vertically and horizontally.
     */
    protected void scrollToCenter(WebElement element) {
        DriverRunner.executeJS("arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});", element);
    }

    /**
    * Creates and returns an {@link ElementWait} bound to this element and the current driver.
    * A new wait is created on each call, so the wait uses the driver and configuration active
    * when the method is invoked.
     *
     * @return this element's {@link ElementWait}
     */
    @Override
    public ElementWait waits() {
        return new ElementWait(this);
    }

    /**
     * Checks whether this element is currently enabled.
     *
     * @return {@code true} if the element is enabled
     */
    @Override
    public boolean isEnabled() {
        return RetryAction.retry(() -> RetryAction.readyCheck(findElement()).isEnabled(), RetryAction.COMMON_EXCEPTIONS,
            () -> "check if element " + locator + " is enabled");
    }

     /**
     * Checks whether this element is currently disabled.
     *
     * @return {@code true} if the element is disabled
     */
    @Override
    public boolean isDisabled() {
        return RetryAction.retry(() -> !RetryAction.readyCheck(findElement()).isEnabled(), RetryAction.COMMON_EXCEPTIONS,
            () -> "check if element " + locator + " is disabled");
    }

    /**
     * Gets the number of matching elements in the DOM. This is equivalent to {@code findElements().size()}.
     * 
     * @return the number of matching elements
     */
    @Override
    public int getSize() {
        return RetryAction.retry(() -> RetryAction.readyCheck(findElements()).size(), RetryAction.COMMON_EXCEPTIONS,
            () -> "get size of elements " + locator);
    }
}
