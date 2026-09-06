package com.sele3.waits;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sele3.drivers.DriverRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryAction {

    /**
     * Transient failures common to any element lookup: the DOM node was replaced between
     * locating the element and acting on it ({@link StaleElementReferenceException}), or the
     * element hasn't appeared in the DOM yet ({@link NoSuchElementException}).
     */
    public static final List<Class<? extends Throwable>> COMMON_EXCEPTIONS =
        List.of(
            StaleElementReferenceException.class,
            NoSuchElementException.class);

    /**
     * {@link #COMMON_EXCEPTIONS} plus failures specific to clicking: the element is covered by
     * another element ({@link ElementClickInterceptedException}) or isn't in an interactable
     * state yet ({@link ElementNotInteractableException}). Used for click/select-style actions.
     */
    public static final List<Class<? extends Throwable>> CLICK_EXCEPTIONS =
        extend(COMMON_EXCEPTIONS, ElementClickInterceptedException.class, ElementNotInteractableException.class);

    /**
     * {@link #CLICK_EXCEPTIONS} plus {@link InvalidElementStateException}, which
     * {@code clear()}/{@code sendKeys()} can throw when the element isn't yet in a state that
     * accepts input (e.g. still disabled or read-only).
     */
    public static final List<Class<? extends Throwable>> SEND_KEYS_EXCEPTIONS =
        extend(CLICK_EXCEPTIONS, InvalidElementStateException.class);

    @SafeVarargs
    private static List<Class<? extends Throwable>> extend(
            List<Class<? extends Throwable>> base, Class<? extends Throwable>... additional) {
        return Stream.concat(base.stream(), Stream.of(additional)).toList();
    }

    /**
     * Runs the given action under a {@link WebDriverWait} built from the current driver's
     * configured timeout and polling interval, ignoring the given exception types while polling.
     * If {@code action} throws one of {@code exceptionsToIgnore}, it is retried from scratch
     * (the action is responsible for re-locating any elements it needs internally); any other
     * exception propagates immediately without retrying.
     *
     * @param action the action to run, re-locating any elements it needs internally
     * @param exceptionsToIgnore exception types that should cause a retry instead of failing immediately
     * @param <T> the result type of the action
     * @return the result of {@code action} once it completes without throwing an ignored exception
     * @throws RuntimeException wrapping the {@link TimeoutException} if the action keeps throwing an ignored exception past the timeout
     */
    public static <T> T retry(Supplier<T> action, List<Class<? extends Throwable>> exceptionsToIgnore) {
        WebDriverWait wait = SeleniumWait.getWebDriverWait();
        wait.ignoreAll(exceptionsToIgnore);

        try {
            return wait.until(driver -> action.get());
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout after " + DriverRunner.getConfig().getTimeout(), e);
        }
    }

    /**
     * {@link Runnable} variant of {@link #retry(Supplier, List)}; see that method for the full
     * retry/timeout semantics.
     *
     * @param action the action to run, re-locating any elements it needs internally
     * @param exceptionsToIgnore exception types that should cause a retry instead of failing immediately
     * @throws RuntimeException wrapping the {@link TimeoutException} if the action keeps throwing an ignored exception past the timeout
     */
    public static void retry(Runnable action, List<Class<? extends Throwable>> exceptionsToIgnore) {
        retry(() -> {
            action.run();
            return true;
        }, exceptionsToIgnore);
    }
}
