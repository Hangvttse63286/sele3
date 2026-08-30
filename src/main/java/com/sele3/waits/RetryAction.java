package com.sele3.waits;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryAction {

    /**
     * Runs the given action, retrying it from scratch on {@link StaleElementReferenceException}
     * (the underlying DOM node was replaced between locating the element and acting on it), via
     * {@link SeleniumWait#executeWait(List, java.util.function.Function)} ignoring that exception
     * while polling. Retries until the current driver's configured timeout elapses, polling at its
     * configured polling interval.
     *
     * @param action the action to run, re-locating any elements it needs internally
     * @param <T> the result type of the action
     * @return the result of {@code action} once it completes without a stale reference
     * @throws RuntimeException wrapping the {@link TimeoutException} if the action keeps going stale past the timeout
     */
    public static <T> T retry(Supplier<T> action) {
        AtomicReference<T> result = new AtomicReference<>();
        SeleniumWait.executeWait(List.of(StaleElementReferenceException.class), driver -> {
            result.set(action.get());
            return true;
        });
        return result.get();
    }

    /**
     * Runs the given action, retrying it from scratch on {@link StaleElementReferenceException}.
     * Retries until the current driver's configured timeout elapses, polling at its configured
     * polling interval.
     *
     * @param action the action to run, re-locating any elements it needs internally
     * @throws RuntimeException wrapping the {@link TimeoutException} if the action keeps going stale past the timeout
     */
    public static void retry(Runnable action) {
        retry(() -> {
            action.run();
            return null;
        });
    }
}
