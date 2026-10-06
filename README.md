# 🚀 SELE3 — Selenium Automation Framework

A Java UI test automation framework built on **Selenium 4** and **AssertJ**. It wraps the
repetitive parts of Selenium (driver setup, waiting, retrying, reporting, soft-assertion bookkeeping)
so test code can stay short and readable, and so tests run reliably in parallel, across browsers and in CI.

---

## Contents

- [Purpose](#purpose)
- [Features](#features)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [How it works](#how-it-works)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [Writing tests](#writing-tests)
- [Reports](#reports)
- [CI/CD (GitHub Actions)](#cicd-github-actions)
- [Extending the framework](#extending-the-framework)
- [Guidelines and best practices](#guidelines-and-best-practices)
- [Troubleshooting](#troubleshooting)

---

## Purpose

Plain Selenium tests tend to fill up with `WebDriverWait`s, `try/catch` retries for stale elements,
manual screenshots and report calls. This framework moves all of that into reusable building blocks:

| Goal | How the framework delivers it |
|---|---|
| Easy to use | One `Element` type with built-in waits; `Assert`/`softly` entry points with AssertJ's fluent API |
| Cross-browser | Chrome, Firefox and Edge out of the box; local or remote (Selenium Grid) |
| Auto-wait for interactions | Every action/accessor retries transient Selenium errors until the configured timeout |
| Auto-retry assertions | Element assertions poll until the condition holds or the timeout expires |
| Parallel execution | Driver, report and soft-assertion state are all thread-local |
| Extensibility | New browsers and reporters plug in via `ServiceLoader`; the test lifecycle is framework-agnostic |
| CI/CD integration | Ready-made GitHub Actions workflow with browser/suite/report/timeout inputs |

---

## Features

- **Element wrapper** (`Element`) — click, type, select, hover, read text/attributes… each one waits for
  the element to be ready and retries stale/intercepted/not-interactable errors.
- **Waits** (`element.waits()`) — `untilVisible`, `untilInvisible`, `untilNotExist`, `untilTextEquals`,
  `untilAttributeContains`, … with a per-call `withTimeout(...)`.
- **Assertions** built on **AssertJ**:
  - `Assert.assertThat(...)` — fail-fast; supports every AssertJ type plus web elements.
  - `softly.assertThat(...)` — soft; failures are collected and checked **automatically** after each test.
  - Element assertions (`isVisible`, `hasText`, `containsAttribute`, …) auto-retry until the timeout.
- **Reporting** with **Allure** or **Extent** (selected at run time): test entries, steps, failure
  screenshots and soft-assertion failures are recorded automatically.
- **Configuration** from a JSON file, overridable by `-D` system properties.
- **Parallel-safe**: one driver, one report entry and one soft-assertion collector per thread.

---

## Tech stack

| Component | Version |
|---|---|
| Java | 21 |
| Maven | 3.8+ |
| Selenium | 4.48.0 (Selenium Manager downloads browser drivers automatically) |
| TestNG | 7.10.1 |
| AssertJ | 3.27.7 |
| Allure TestNG adapter / Allure report | 2.35.5 / 3.19.1 |
| ExtentReports | 5.1.2 |
| Lombok, Gson, SLF4J | — |

---

## Project structure

```
sele3
├── .github/workflows/ui-tests.yml        # CI pipeline (GitHub Actions)
├── pom.xml                               # dependencies, surefire, plugins
├── src/main/java/com/sele3               # ── the framework ──
│   ├── adapters/      Gson adapters for Duration, Capabilities, IPlatform
│   ├── asserts/       Assert (hard), SoftAssert (softly), ElementAssert (element checks)
│   ├── configs/       Configuration model, ConfigLoader (JSON + -D overrides), ConfigKey names
│   ├── drivers/       DriverRunner (public API), DriverContainer (thread-local driver),
│   │                  DriverFactory + Chrome/Firefox/Edge factories, Platform
│   ├── elements/      BaseElement (contract) and Element (implementation)
│   ├── lifecycle/     ITestLifecycle: start/end report entry + automatic soft assertAll
│   ├── listeners/     TestNgListener: TestNG adapter for ITestLifecycle
│   ├── reports/       ReportRunner (public API), ReportContainer (thread-local reporter),
│   │                  Allure/Extent factories, ReportStatus, ReportType
│   └── waits/         SeleniumWait, ElementWait, RetryAction, RetryableExceptions
└── src/main/resources
    ├── configs/                          # run configuration per browser: chrome.json, edge.json, firefox.json
    └── META-INF/services/                # ServiceLoader registrations (browsers, reporters)
```

---

## How it works

```
Test phase                 Framework                                  Browser / Report
──────────                 ─────────                                  ────────────────
Setup              ──────► DriverRunner.open()  ── loads config ────► starts browser (thread-local)
Test starts        ──────► ITestLifecycle.startTest() ─────────────► report entry started
Test body          ──────► Element actions / waits / ReportRunner.step / Assert / softly
Test body finished ──────► ITestLifecycle.endTest()
                             ├─ softly.assertAll()  → soft failures fail the test
                             └─ ReportRunner.endTest(PASS|FAIL) ───► report entry finished
Test skipped       ──────► ITestLifecycle.skipTest() ──────────────► report entry = SKIP
Teardown           ──────► DriverRunner.close() ───────────────────► browser quit
```

Key points:

- **Thread-local state** — `DriverContainer`, `ReportContainer` and `SoftAssert` keep one instance per
  thread, so tests can run in parallel.
- **Lifecycle is framework-agnostic** — `ITestLifecycle` contains the logic; `TestNgListener` is only a
  thin adapter. Another runner (e.g. JUnit 5) only needs its own adapter.
- **Cleanup never hides the real error** — failures while taking screenshots, writing the report or
  quitting the browser are logged, not thrown, so the test's own error is what you see.

---

## Getting started

### Prerequisites

- JDK 21
- Maven 3.8+
- Chrome, Firefox or Edge installed (drivers are resolved automatically by Selenium Manager)

### Run a suite

Point `suiteXmlFile` at your TestNG suite XML:

```bash
mvn clean test -DsuiteXmlFile=path/to/suite.xml                         # Chrome, no report
mvn clean test -DsuiteXmlFile=path/to/suite.xml -DreportType=allure     # with Allure results
mvn clean test -DsuiteXmlFile=path/to/suite.xml -DreportType=extent -Dplatform=firefox -Dheadless=true
```

> Locally, test failures **don't** fail the Maven build (`maven.test.failure.ignore=true` in the POM) so
> the reports are always produced. CI overrides it with `-Dmaven.test.failure.ignore=false`.

---

## Configuration

Configuration is loaded from a JSON file in `src/main/resources/configs/`, then any `-D` system
property overrides the matching field. Fields missing from both fall back to a built-in default.

One config file is provided per browser — `chrome.json`, `edge.json`, `firefox.json` — each with its
own `platform` and browser-specific `capabilities`. The file follows the platform automatically:

```bash
mvn test                              # chrome.json
mvn test -Dplatform=firefox           # firefox.json
mvn test -DconfigFileName=my.json     # an explicit file always wins
```

If the file doesn't exist in your project's `src/main/resources/configs/` (or under
`-DconfigSourcePath`), the framework's bundled default with the same name is used, and the log says
`Using config from the classpath: <location>`. If neither exists, the run fails with an error naming
both places it looked. A file of your own replaces the default
entirely; fields it leaves out fall back to the built-in defaults below, not to the framework's file.

A new platform (see [Add a browser](#add-a-browser)) loads `<platform name in lower case>.json` by
default; override `IPlatform.getDefaultConfigFile()` to use another name.

| Key | Meaning | Default |
|---|---|---|
| `platform` | `chrome`, `firefox`, `edge` (or any registered platform) | `chrome` |
| `headless` | Run without a visible browser window | `false` |
| `startMaximized` | Maximize the window (ignored in headless) | `false` |
| `windowSize` | Window size, `width,height` | `1920,1080` |
| `pageLoadStrategy` | `normal`, `eager`, `none` | `normal` |
| `timeout` | Max wait for waits, retries and element assertions (**ms**) | `60000` |
| `pollingInterval` | How often waits re-check (**ms**) | `500` |
| `baseUrl` | URL opened by `DriverRunner.open()` | `http://localhost:8080` |
| `remote` | Use a remote WebDriver (Selenium Grid) | `false` |
| `remoteUrl` | Grid URL | `http://localhost:4444/wd/hub` |
| `capabilities` | Extra capabilities, JSON (e.g. `goog:chromeOptions`) | `{}` |

Run-time only properties:

| Property | Meaning |
|---|---|
| `reportType` | `allure` or `extent`; unset = no report |
| `suiteXmlFile` | TestNG suite XML to run (default set by the `suiteXmlFile` property in `pom.xml`) |
| `configSourcePath` / `configFileName` | Load a different config file (defaults `src/main/resources/configs/` + `<platform>.json`, i.e. `chrome.json` when no platform is set) |
| `extent.report.path` | Extent output file (default `target/reports/extent-reports/index.html`) |

Example:

```bash
mvn test -DconfigFileName=staging.json -Dtimeout=30000 -DbaseUrl=https://staging.example.com
```

---

## Writing tests

### 1. Test class skeleton (TestNG)

```java
@Listeners(TestNgListener.class)          // reports + automatic soft assertAll
public class SearchTest {

    @BeforeClass(alwaysRun = true)
    public void openBrowser() {
        DriverRunner.open();                  // starts the browser (if needed) and opens baseUrl
    }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        DriverRunner.close();                 // quits the browser
    }

    @Test(description = "Searching for a product shows matching results")   // shown in the report
    public void searchShowsResults() { ... }
}
```

- Always attach `TestNgListener` (with `@Listeners` or in the suite XML); without it there is no report
  and soft assertions are never checked.
- Open and close the browser around each test class, typically in a shared base class.

### 2. Elements

Declare elements as fields with a `By` locator, or with an XPath template for dynamic elements:

```java
private final BaseElement loginButton = new Element(By.cssSelector("button[name='login']"));
private final BaseElement productCard = new Element("//li[contains(@class,'product')][.//h2[text()='%s']]");

productCard.set("Blue Shirt");   // resolves the template before use
productCard.click();
```

Common actions — all wait and retry automatically:

| Category | Methods |
|---|---|
| Mouse | `click()`, `doubleClick()`, `rightClick()`, `hover()`, `clickViaJS()` |
| Keyboard | `enter(...)`, `clearAndEnter(...)` |
| Select | `select(text)`, `selectByValue(v)`, `selectByIndex(i)`, `getSelectedOption()` |
| Read | `getText()`, `getValue()`, `getAttribute(name)`, `getCssValue(name)`, `getAllTexts()`, `getSize()` |
| State | `isEnabled()`, `isDisabled()`, `isSelected()` |
| Scroll | `scrollToView()`, `scrollToCenter()` |

### 3. Waits

Use explicit waits only when an action must wait for something that is **not** the element itself
(e.g. a spinner disappearing):

```java
spinner.waits().untilNotExist();                                   // removed from the DOM
toast.waits().withTimeout(Duration.ofSeconds(5)).untilInvisible(); // present but hidden
new SeleniumWait().untilUrlContains("/checkout/");
```

| Wait | Condition |
|---|---|
| `untilExist` / `untilNotExist` | present in / removed from the DOM |
| `untilVisible` / `untilInvisible` | displayed / present but none displayed |
| `untilClickable`, `untilEnabled`, `untilDisabled` | interactability |
| `untilTextEquals/NotEquals/Contains` | visible text |
| `untilValue…`, `untilAttribute…` | `value` / any attribute |
| `untilChecked`, `untilUnchecked` | selection state |
| `untilElementsAreExactly(n)`, `untilElementsAreAtLeast(n)` | match count |

`null` passed to `withTimeout` keeps the configured timeout.

### 4. Assertions

**Hard assertions** stop the test at the first failure:

```java
import com.sele3.asserts.Assert;

Assert.assertThat(DriverRunner.getCurrentUrl()).as("Current URL").contains("/search");
Assert.assertThat(resultList).as("Search results").isVisible();
Assert.assertThat(resultHeader).as("Results header").containsText("results for");
```

**Soft assertions** keep the test running and fail it at the end with every collected failure:

```java
import static com.sele3.asserts.SoftAssert.softly;

softly.assertThat(DriverRunner.getPageTitle()).as("Page title").startsWith("My Shop");
softly.assertThat(searchBox).as("Search box").isVisible();
// no assertAll() needed — the listener calls it after the test
```

- `softly` is a shared, thread-safe instance; there is nothing to create or declare.
- Each soft failure is logged as a **FAIL step** in the report immediately.
- All AssertJ assertions are available (`String`, numbers, collections, objects, …).

#### Required: connect the test lifecycle

Soft assertions are only checked, and report entries only started and finished, if your test
framework calls `ITestLifecycle` around every test. Implement `ITestLifecycle` in your framework's
hook mechanism (listener, extension, fixture, …) and call:

| When | Call | Why |
|---|---|---|
| A test starts | `startTest(name, description)` | Starts the report entry |
| The test body has finished, **while the framework can still change the result** | `Throwable error = endTest(thrownOrNull)`, then fail the test with `error` if it isn't `null` | Runs `softly.assertAll()` and ends the report entry as PASS/FAIL |
| A test is skipped/aborted | `skipTest(reason)` | Ends the report entry as SKIP and discards pending soft failures |

If `endTest` isn't called, soft-assertion failures are silently lost, or leak into the next test on
the same thread. If it's called too late (after the framework has recorded the result), soft
failures are reported but don't fail the test.

**TestNG** — already provided, just attach it:

```java
@Listeners(TestNgListener.class)
public class SearchTest { ... }
```

or once for the whole suite in the suite XML:

```xml
<listeners>
    <listener class-name="com.sele3.listeners.TestNgListener"/>
</listeners>
```

**JUnit 5** — an example extension (not shipped with the framework). `interceptTestMethod` wraps the
test body, so it can still turn soft failures into a test failure:

```java
public class Sele3Extension implements BeforeEachCallback, InvocationInterceptor, ITestLifecycle {

    @Override
    public void beforeEach(ExtensionContext context) {
        startTest(context.getDisplayName(), null);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext context) throws Throwable {
        Throwable thrown = null;
        try {
            invocation.proceed();
        } catch (Throwable t) {
            thrown = t;
        }
        if (thrown instanceof TestAbortedException) {   // Assumptions.assume…() failed
            skipTest(thrown);
            throw thrown;
        }
        Throwable error = endTest(thrown);
        if (error != null) {
            throw error;
        }
    }
}

@ExtendWith(Sele3Extension.class)
class SearchTest { ... }
```

For any other framework, follow the same pattern; `TestNgListener` is the reference implementation.

**Element assertions** (available from both `Assert` and `softly`) poll until they pass or time out:

| Assertion | |
|---|---|
| `isAttached()`, `isVisible()`, `isHidden()` | presence / visibility |
| `isInteractable()`, `isEnabled()`, `isDisabled()` | interactability |
| `isChecked()`, `isUnchecked()` | selection |
| `hasText`, `doesNotHaveText`, `containsText` | visible text |
| `hasValue`, `doesNotHaveValue`, `containsValue` | `value` attribute |
| `hasAttribute`, `doesNotHaveAttribute`, `containsAttribute` | any attribute |

```java
Assert.assertThat(banner).withTimeout(Duration.ofSeconds(5)).as("Promo banner").isVisible();
```

A failure reads like `[Promo banner] Expecting element to be visible (waited up to 5000 ms)`. Only the
wait **expiring** counts as an assertion failure; a real Selenium error (e.g. a browser crash or a
script timeout) is rethrown unchanged so it isn't hidden as a mismatch.

### 5. Report steps

```java
ReportRunner.step("Enter username", () -> username.clearAndEnter("user"));  // FAIL + screenshot if it throws
ReportRunner.step(ReportStatus.INFO, "Cart is empty, skipping clean-up");
ReportRunner.log(ReportStatus.WARNING, "Using fallback product");
ReportRunner.attachScreenshot("After checkout");
ReportRunner.attachText("Order payload", json);
```

All `ReportRunner` calls are no-ops when no `reportType` is set, so tests run the same with or without a report.

---

## Reports

| Report | Enable with | Output |
|---|---|---|
| Allure | `-DreportType=allure` | raw results: `target/reports/allure-reports/` |
| Extent | `-DreportType=extent` | `target/reports/extent-reports/index.html` |
| Surefire/TestNG | always | `target/surefire-reports/` |

Generate the Allure HTML report (a single self-contained `index.html`):

```bash
mvn allure:report        # → target/reports/allure-html/index.html
```

The first run downloads Node.js and Allure into `.allure/` (git-ignored). The Allure report version is
pinned to `3.19.1` in the POM because 3.4.1 renders blank test pages.

What is recorded automatically:

- One entry per test, with its description, final status and error;
- Skipped tests (including those TestNG skips without running) with the skip reason;
- Each soft-assertion failure as a FAIL step;
- A screenshot when a `ReportRunner.step(name, action)` fails or a failure-status step is logged.

---

## CI/CD (GitHub Actions)

`.github/workflows/ui-tests.yml` runs the suite on `ubuntu-latest` (headless):

- **Triggers**: every pull request to `main`, and manually from **Actions → UI tests → Run workflow**.
- **Manual inputs**:

  | Input | Values | Default |
  |---|---|---|
  | `suite` | suite XML path | the workflow's default suite |
  | `reportType` | `allure`, `extent` | `allure` |
  | `platform` | `chrome`, `firefox`, `edge` | `chrome` |
  | `timeout` | number (ms), optional | from config |

- **Result**: the run fails if any test fails; a pass/fail summary is written to the run page.
- **Artifacts**: `reports-<platform>-<reportType>-run<N>` containing the Allure single-file
  `index.html` (or the Extent report) plus the Surefire reports.

---

## Extending the framework

### Add a browser

1. Implement `IDriverFactory<YourOptions>` (public no-arg constructor) and return a platform from
   `getPlatform()` — an enum implementing `IPlatform`, e.g. `SAFARI`.
2. Register it in `src/main/resources/META-INF/services/com.sele3.drivers.IDriverFactory`.
3. Add its config file, `src/main/resources/configs/safari.json`.
4. Run with `-Dplatform=safari`. No framework code changes needed.

### Add a reporter

1. Implement `IReportFactory` (public no-arg constructor) and return an `IReportType` from
   `getReportType()`.
2. Register it in `src/main/resources/META-INF/services/com.sele3.reports.IReportFactory`.
3. Run with `-DreportType=<name>`.

### Use another test runner

Implement `ITestLifecycle` in that runner's listener/extension. See
[Required: connect the test lifecycle](#required-connect-the-test-lifecycle) for when to call
`startTest`, `endTest` and `skipTest`, and a JUnit 5 example. `TestNgListener` is the reference
implementation.

---

## Guidelines and best practices

**Test lifecycle**
- Every test must run through `ITestLifecycle`: implement it in your test framework's
  listener/extension/fixture and call `startTest()` when a test starts, `endTest()` when its body
  finishes (while the result can still change) and `skipTest()` when it's skipped. With TestNG, just
  attach `TestNgListener`. See [Required: connect the test lifecycle](#required-connect-the-test-lifecycle).
- Register it once for the whole suite (suite XML, base class or global extension) rather than per
  class, so no test can run without it.

**Test design**
- Keep tests independent: no shared state between test methods or classes, no ordering assumptions.
- One behaviour per test; use a meaningful `@Test(description = ...)` — it becomes the report title.
- Don't put tests that must share a browser session in different classes; the browser is per class.

**Elements and locators**
- Prefer stable locators: `id` > `name` > dedicated `data-*` attributes > CSS > XPath.
- Declare elements in page objects.
- Use XPath templates + `set(...)` for elements that differ only by a value (row, product name, …).

**Waiting**
- **Never** use `Thread.sleep`. Actions and element assertions already wait.
- Add an explicit `waits()` call only for conditions not covered by the action itself.
- Override the timeout per call (`withTimeout`) instead of changing the global one.

**Assertions**
- Always add `.as("...")` — it is the first thing you read in a failure message and in the report.
- Use `Assert` when the rest of the test makes no sense after a failure (e.g. login failed);
  use `softly` to check many independent things on one page.
- Assert on elements with `assertThat(element).isVisible()` rather than
  `assertThat(element.isDisplayed()).isTrue()` — the former retries, the latter checks once.
- Don't call `softly.assertAll()` yourself; `ITestLifecycle.endTest()` does it.

**Reporting**
- Wrap meaningful user actions in `ReportRunner.step("...", () -> ...)` so the report reads like a test script.
- Don't log secrets (passwords, tokens) in step names or attachments.

**Code style**
- Java 21, Lombok `@Slf4j` for class logging, Javadoc on public API.
- New framework code goes under `src/main/java/com/sele3`.

---

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `No driver is bound to current thread` | `DriverRunner.open()` wasn't called before the test, or code runs before `@BeforeClass`. |
| No report produced | `-DreportType` not set, or the listener isn't attached (`@Listeners(TestNgListener.class)`). |
| `Unknown report type` / `Unknown platform` | Typo in `-DreportType` / `-Dplatform`, or the factory isn't registered in `META-INF/services`. |
| Allure test pages are blank | Use the pinned report version (`3.19.1`); delete `.allure/` to force a re-download. |
| Element assertions are slow to fail | They wait up to `timeout`; lower it per call with `withTimeout(...)`. |
| Soft failures appear in the next test | The listener isn't attached, so `assertAll()` never ran for that test. |
