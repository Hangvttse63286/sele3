package com.sele3.waits;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sele3.drivers.DriverRunner;

import lombok.extern.slf4j.Slf4j;

/**
 * Retry helpers for transient Selenium failures during element interactions.
 *
 * <p>{@link RetryableExceptions} provides exception lists that can be passed to
 * {@link #retry(Supplier, List)} or {@link #retry(Runnable, List)} to express which failures
 * should be retried. Actions should locate elements inside the supplied callback so each retry
 * uses the current DOM node.</p>
 */
@Slf4j
public class RetryAction {

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
        return retry(action, exceptionsToIgnore, () -> "waiting for action to succeed");
    }

    /**
     * {@link #retry(Supplier, List)} variant that describes what is being waited for, so a
     * timeout failure reports more than a generic message. The description is evaluated lazily,
     * only if the wait actually times out, so it can reference live state (e.g. a locator).
     *
     * @param action the action to run, re-locating any elements it needs internally
     * @param exceptionsToIgnore exception types that should cause a retry instead of failing immediately
     * @param description describes what {@code action} is waiting for, evaluated only on timeout
     * @param <T> the result type of the action
     * @return the result of {@code action} once it completes without throwing an ignored exception
     * @throws RuntimeException wrapping the {@link TimeoutException} if the action keeps throwing an ignored exception past the timeout
     */
    public static <T> T retry(Supplier<T> action, List<Class<? extends Throwable>> exceptionsToIgnore, Supplier<String> description) {
        SeleniumWait wait = new SeleniumWait();
        wait.ignoreAll(exceptionsToIgnore);
        wait.withMessage(description);
        AtomicReference<T> result = new AtomicReference<>();
        try {
            // Wrapped so a legitimate false/null result from action isn't mistaken by
            // WebDriverWait.until() for "condition not yet satisfied" and retried until timeout.
            wait.until(driver -> {
                result.set(action.get());
                return true;
            });
        } catch (TimeoutException e) {
            throw new RuntimeException(description.get() + " - timeout after " + DriverRunner.getConfig().getTimeout(), e);
        }
        return result.get();
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

    /**
     * {@link Runnable} variant of {@link #retry(Supplier, List, Supplier)}; see that method for
     * the full retry/timeout semantics.
     *
     * @param action the action to run, re-locating any elements it needs internally
     * @param exceptionsToIgnore exception types that should cause a retry instead of failing immediately
     * @param description describes what {@code action} is waiting for, evaluated only on timeout
     * @throws RuntimeException wrapping the {@link TimeoutException} if the action keeps throwing an ignored exception past the timeout
     */
    public static void retry(Runnable action, List<Class<? extends Throwable>> exceptionsToIgnore, Supplier<String> description) {
        retry(() -> {
            action.run();
            return true;
        }, exceptionsToIgnore, description);
    }

    /**
     * Throws if {@code result} indicates the checked condition isn't satisfied yet ({@code null}
     * or {@code false}), so a caller inside {@link #retry} can trigger a retry by simply calling
     * this instead of writing its own null/false check.
     *
     * @param result the value to check
     * @param <T> the type of {@code result}
     * @return {@code result}, unchanged, if it isn't {@code null} or {@code false}
     * @throws NoSuchElementException if {@code result} is {@code null} or {@code false}
     */
    public static <T> T readyCheck(T result) {
        if (result == null || Boolean.FALSE.equals(result)) {
            throw new NoSuchElementException("Condition not yet satisfied for locator");
        }
        return result;
    }
}
