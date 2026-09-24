package listeners;

import org.testng.ITestListener;
import org.testng.ITestResult;

import com.sele3.reports.ReportRunner;
import com.sele3.reports.ReportStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * TestNG listener that wires {@link ReportRunner}'s test lifecycle to TestNG's own: starts a
 * report entry when a test method begins, ends it with the matching {@link ReportStatus} when it
 * finishes, and — on failure — logs the exception.
 */
@Slf4j
public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        ReportRunner.startTest(result.getMethod().getMethodName(), result.getMethod().getDescription());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ReportRunner.endTest(ReportStatus.PASS);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            ReportRunner.logException(throwable);
        }
        ReportRunner.endTest(ReportStatus.FAIL);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ReportRunner.endTest(ReportStatus.SKIP);
    }
}
