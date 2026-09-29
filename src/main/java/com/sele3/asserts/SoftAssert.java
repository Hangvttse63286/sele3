package com.sele3.asserts;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.StandardSoftAssertionsProvider;

import com.sele3.elements.BaseElement;
import com.sele3.reports.ReportRunner;
import com.sele3.reports.ReportStatus;

/**
 * Entry point for soft assertions: {@code softly.assertThat(actual).isEqualTo(expected)} records a
 * failure (and reports it as a FAIL step) instead of throwing, letting the test keep running.
 * {@link com.sele3.lifecycle.ITestLifecycle#endTest} calls {@link #assertAll()} after each test, so
 * tests don't have to. For fail-fast assertions, see {@link Assert}.
 *
 * <pre>{@code
 * import static com.sele3.asserts.SoftAssert.softly;
 *
 * softly.assertThat(title).as("Page title").startsWith("TestArchitect");
 * softly.assertThat(cartLink).as("Cart link").isVisible();
 * }</pre>
 *
 * <p>{@link #softly} is one shared, stateless instance: every AssertJ soft {@code assertThat} comes
 * from {@link StandardSoftAssertionsProvider} and collects into the current thread's own
 * {@link SoftAssertions}, so nothing needs to be instantiated and parallel tests stay isolated.
 */
public final class SoftAssert implements StandardSoftAssertionsProvider {
    public static final SoftAssert softly = new SoftAssert();

    private static final ThreadLocal<SoftAssertions> current = ThreadLocal.withInitial(() -> new SoftAssertions() {
        @Override
        public void onAssertionErrorCollected(AssertionError error) {
            ReportRunner.step(ReportStatus.FAIL, error.getMessage());
        }
    });

    private SoftAssert() {
    }

    /**
     * Starts a soft assertion on a {@link BaseElement}. Each check polls the element up to the
     * current driver's configured timeout before recording a failure; see {@link ElementAssert}.
     *
     * @param actual the element under assertion
     * @return assertions for {@code actual}
     */
    public ElementAssert assertThat(BaseElement actual) {
        return proxy(ElementAssert.class, BaseElement.class, actual);
    }

    /**
     * Records a failure with the given message without stopping the current test.
     *
     * @param message the failure message
     */
    public void fail(String message) {
        current.get().fail(message);
    }

    /**
     * Fails with every failure collected on the current thread, then clears them so the next test
     * starts clean; does nothing if none are pending.
     *
     * @throws AssertionError if any soft assertion failed on the current thread
     */
    @Override
    public void assertAll() {
        SoftAssertions softAssertions = current.get();
        current.remove();
        softAssertions.assertAll();
    }

    @Override
    public <SELF extends org.assertj.core.api.Assert<? extends SELF, ? extends ACTUAL>, ACTUAL> SELF proxy(
            Class<SELF> assertClass, Class<ACTUAL> actualClass, ACTUAL actual) {
        return current.get().proxy(assertClass, actualClass, actual);
    }

    @Override
    public void collectAssertionError(AssertionError error) {
        current.get().collectAssertionError(error);
    }

    @Override
    public List<AssertionError> assertionErrorsCollected() {
        return current.get().assertionErrorsCollected();
    }

    @Override
    public void succeeded() {
        current.get().succeeded();
    }

    @Override
    public boolean wasSuccess() {
        return current.get().wasSuccess();
    }

    @Override
    public void onAssertionErrorCollected(AssertionError error) {
        current.get().onAssertionErrorCollected(error);
    }
}
