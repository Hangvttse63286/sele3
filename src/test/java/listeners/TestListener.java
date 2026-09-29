package listeners;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.SkipException;

import com.sele3.lifecycle.ITestLifecycle;

/**
 * TestNG adapter for {@link ITestLifecycle}. Starts the report entry when a test starts; ends it
 * right after the test method runs (while TestNG still lets the result change), failing the test on
 * any pending soft-assertion failures; and ends skipped tests as SKIP, including those TestNG skips
 * without running (a failed dependency or setup method).
 */
public class TestListener implements ITestListener, IInvokedMethodListener, ITestLifecycle {

    @Override
    public void onTestStart(ITestResult result) {
        startTest(result.getMethod().getMethodName(), result.getMethod().getDescription());
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        // Skips (SkipException, or a failed dependency/setup method) are ended in onTestSkipped.
        if (!method.isTestMethod() || result.getStatus() == ITestResult.SKIP || result.getThrowable() instanceof SkipException) {
            return;
        }
        Throwable error = endTest(result.getThrowable());
        if (error != null) {
            result.setThrowable(error);
            result.setStatus(ITestResult.FAILURE);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        skipTest(result.getThrowable());
    }
}
