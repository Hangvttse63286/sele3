package com.sele3.waits;

import java.time.Instant;
import java.util.function.Supplier;

import org.openqa.selenium.StaleElementReferenceException;

import com.sele3.drivers.DriverRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryAction {

    /**
     * Runs the given action, retrying it from scratch on {@link StaleElementReferenceException}
     * (the underlying DOM node was replaced between locating the element and acting on it).
     * Retries until the current driver's configured timeout elapses, polling at its configured
     * polling interval.
     *
     * @param action the action to run, re-locating any elements it needs internally
     * @param <T> the result type of the action
     * @return the result of {@code action} once it completes without a stale reference
     * @throws StaleElementReferenceException if the action keeps going stale past the timeout
     */
    public static <T> T retry(Supplier<T> action) {
        Instant deadline = Instant.now().plus(DriverRunner.getConfig().getTimeout());

        while (true) {
            try {
                return action.get();
            } catch (StaleElementReferenceException e) {
                if (Instant.now().isAfter(deadline)) {
                    throw new StaleElementReferenceException("Action kept going stale past timeout of " + DriverRunner.getConfig().getTimeout(), e);
                }
                log.debug("Stale element, retrying", e);
                DriverRunner.sleep(DriverRunner.getConfig().getPollingInterval());
            }
        }
    }

    /**
     * Runs the given action, retrying it from scratch on {@link StaleElementReferenceException}.
     * Retries until the current driver's configured timeout elapses, polling at its configured
     * polling interval.
     *
     * @param action the action to run, re-locating any elements it needs internally
     * @throws StaleElementReferenceException if the action keeps going stale past the timeout
     */
    public static void retry(Runnable action) {
        retry(() -> {
            action.run();
            return null;
        });
    }
}
