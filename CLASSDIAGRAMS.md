# Class Diagram

Class diagrams for the `SELE3` framework (`src/main/java/com/sele3`), written in
[Mermaid](https://mermaid.js.org/syntax/classDiagram.html).

- [Overview](#overview)
- [Lifecycle and listeners](#lifecycle-and-listeners)
- [Assertions](#assertions)
- [Elements and waits](#elements-and-waits)
- [Drivers](#drivers)
- [Configuration](#configuration)
- [Reports](#reports)

---

## Overview

How the packages fit together. Arrows point from the class that depends on another to the class it
depends on.

```mermaid
classDiagram
    direction LR

    class ITestLifecycle {
        <<interface>>
    }
    class TestNgListener
    class SoftAssert
    class Assert
    class ElementAssert
    class ReportRunner
    class ReportContainer
    class IReportFactory {
        <<interface>>
    }
    class DriverRunner
    class DriverContainer
    class Driver
    class DriverFactory
    class IDriverFactory {
        <<interface>>
    }
    class ConfigLoader
    class Configuration
    class BaseElement {
        <<interface>>
    }
    class Element
    class ElementWait
    class SeleniumWait
    class RetryAction

    TestNgListener ..|> ITestLifecycle
    ITestLifecycle ..> ReportRunner : start/end report entry
    ITestLifecycle ..> SoftAssert : assertAll()

    Assert ..> ElementAssert : creates
    SoftAssert ..> ElementAssert : creates soft proxy
    ElementAssert --> BaseElement : actual
    ElementAssert ..> ElementWait : polls with
    SoftAssert ..> ReportRunner : FAIL step

    Element ..|> BaseElement
    Element ..> ElementWait : waits()
    Element ..> RetryAction : actions
    ElementWait --|> SeleniumWait
    RetryAction ..> SeleniumWait

    ReportRunner --> ReportContainer
    ReportContainer --> IReportFactory : per thread
    IReportFactory ..> DriverRunner : screenshots

    DriverRunner --> DriverContainer
    DriverRunner ..> ConfigLoader : open()
    DriverContainer --> Driver : per thread
    Driver --> Configuration
    Driver ..> DriverFactory : createDriver()
    DriverFactory ..> IDriverFactory : ServiceLoader
    ConfigLoader ..> Configuration : creates
```

**Main flows**

- **Test lifecycle:** the test runner's adapter (`TestNgListener`) calls `ITestLifecycle`, which
  starts/ends the report entry through `ReportRunner` and checks soft assertions through
  `SoftAssert.softly.assertAll()`.
- **Browser:** `DriverRunner.open()` loads a `Configuration` with `ConfigLoader`, and
  `DriverContainer` keeps one `Driver` per thread. `Driver` asks `DriverFactory` for a `WebDriver`,
  which picks the `IDriverFactory` registered for the configured platform.
- **Reporting:** `ReportRunner` is the static API used by tests and the framework; `ReportContainer`
  keeps one `IReportFactory` (Allure or Extent) per thread.
- **Elements and assertions:** `Element` actions retry through `RetryAction`; element assertions
  (`ElementAssert`) poll through `ElementWait`.

---

## Lifecycle and listeners

Packages: `com.sele3.lifecycle`, `com.sele3.listeners`

```mermaid
classDiagram
    class ITestLifecycle {
        <<interface>>
        +startTest(String name, String description) void
        +endTest(Throwable testError) Throwable
        +skipTest(Throwable reason) void
        -endReport(IReportStatus status, Throwable error) void
    }

    class TestNgListener {
        +onTestStart(ITestResult result) void
        +afterInvocation(IInvokedMethod method, ITestResult result) void
        +onTestSkipped(ITestResult result) void
    }

    class ITestListener {
        <<interface>>
        TestNG
    }
    class IInvokedMethodListener {
        <<interface>>
        TestNG
    }

    TestNgListener ..|> ITestLifecycle
    TestNgListener ..|> ITestListener
    TestNgListener ..|> IInvokedMethodListener
    ITestLifecycle ..> ReportRunner
    ITestLifecycle ..> SoftAssert
```

| Method | Called when | Does |
|---|---|---|
| `startTest` | a test starts | starts the report entry (a reporter error is logged, not thrown) |
| `endTest` | the test body finished, while the result can still change | runs `softly.assertAll()`, ends the report entry as PASS/FAIL, returns the error the test should fail with (or `null`) |
| `skipTest` | a test is skipped | discards pending soft failures, ends the report entry as SKIP |

---

## Assertions

Package: `com.sele3.asserts`

```mermaid
classDiagram
    class Assertions {
        AssertJ
    }
    class AbstractAssert {
        AssertJ
    }
    class StandardSoftAssertionsProvider {
        <<interface>>
        AssertJ
    }
    class SoftAssertions {
        AssertJ
    }

    class Assert {
        <<final>>
        -Assert()
        +assertThat(BaseElement actual)$ ElementAssert
    }

    class SoftAssert {
        <<final>>
        +SoftAssert softly$
        -ThreadLocal~SoftAssertions~ current$
        -SoftAssert()
        +assertThat(BaseElement actual) ElementAssert
        +fail(String message) void
        +assertAll() void
        +proxy(Class assertClass, Class actualClass, ACTUAL actual) SELF
        +collectAssertionError(AssertionError error) void
        +assertionErrorsCollected() List~AssertionError~
        +succeeded() void
        +wasSuccess() boolean
        +onAssertionErrorCollected(AssertionError error) void
    }

    class ElementAssert {
        -Duration timeout
        +ElementAssert(BaseElement actual)
        +withTimeout(Duration timeout) ElementAssert
        +isAttached() ElementAssert
        +isVisible() ElementAssert
        +isHidden() ElementAssert
        +isInteractable() ElementAssert
        +isEnabled() ElementAssert
        +isDisabled() ElementAssert
        +isChecked() ElementAssert
        +isUnchecked() ElementAssert
        +hasText(String text) ElementAssert
        +doesNotHaveText(String text) ElementAssert
        +containsText(String text) ElementAssert
        +hasValue(String value) ElementAssert
        +doesNotHaveValue(String value) ElementAssert
        +containsValue(String value) ElementAssert
        +hasAttribute(String attribute, String value) ElementAssert
        +doesNotHaveAttribute(String attribute, String value) ElementAssert
        +containsAttribute(String attribute, String value) ElementAssert
        -check(Consumer~ElementWait~ waitAction, String expectation) ElementAssert
    }

    Assertions <|-- Assert
    StandardSoftAssertionsProvider <|.. SoftAssert
    AbstractAssert <|-- ElementAssert
    SoftAssert --> SoftAssertions : one per thread
    Assert ..> ElementAssert : creates
    SoftAssert ..> ElementAssert : creates soft proxy
    ElementAssert --> BaseElement : actual
    ElementAssert ..> ElementWait : polls with
```

- `Assert` inherits every AssertJ `assertThat(...)` and adds one for `BaseElement`.
- `SoftAssert.softly` is a single shared instance; each thread collects into its own
  `SoftAssertions`, and every collected failure is reported as a FAIL step.
- `ElementAssert.check` creates a new `ElementWait` per check. Only that wait expiring
  (`SeleniumWait.getExpiry()`) becomes an assertion failure; other exceptions are rethrown.

---

## Elements and waits

Packages: `com.sele3.elements`, `com.sele3.waits`

```mermaid
classDiagram
    class BaseElement {
        <<interface>>
        +set(Object... args)* void
        +getBy()* By
        +getAttribute(String attribute)* String
        +getText()* String
        +getInnerText()* String
        +getValue()* String
        +getCssValue(String name)* String
        +getAllCssValues(String name)* List~String~
        +getAllTexts()* List~String~
        +click()* void
        +rightClick()* void
        +doubleClick()* void
        +hover()* void
        +clickViaJS()* void
        +enter(CharSequence... values)* void
        +clearAndEnter(CharSequence... values)* void
        +select(String option)* void
        +selectByValue(String value)* void
        +selectByIndex(int index)* void
        +getSelectedOption()* String
        +getAllSelectedOptions()* List~String~
        +scrollToView()* void
        +scrollToCenter()* void
        +waits()* ElementWait
        +isEnabled()* boolean
        +isDisabled()* boolean
        +isSelected()* boolean
        +getSize()* int
    }

    class Element {
        #By locator
        #String dynamicXPathLocator
        +Element(By locator)
        +Element(String dynamicXPathLocator)
        #findElement() WebElement
        #findInteractableElement() WebElement
        #findElements() List~WebElement~
        #scrollToView(WebElement element) void
        #scrollToCenter(WebElement element) void
    }

    class WebDriverWait {
        Selenium
    }

    class SeleniumWait {
        -RuntimeException expiry
        +SeleniumWait()
        +SeleniumWait(Duration timeout, Duration pollingInterval)
        +getTimeout() Duration
        +withTimeout(Duration timeout) SeleniumWait
        +getExpiry() RuntimeException
        #timeoutException(String message, Throwable lastException) RuntimeException
        +untilPageToLoad() void
        +untilPageToLoadingAndComplete() void
        +untilJQueryToLoad() void
        +untilJQueryToProcessAndLoad() void
        +untilUrlContains(String text) void
    }

    class ElementWait {
        -BaseElement element
        +ElementWait(BaseElement element)
        +ElementWait(BaseElement element, Duration timeout, Duration pollingInterval)
        +withTimeout(Duration timeout) ElementWait
        +getElement() BaseElement
        +untilExist() void
        +untilAllExist() void
        +untilNotExist() void
        +untilVisible() void
        +untilAllVisible() void
        +untilInvisible() void
        +untilClickable() void
        +untilEnabled() void
        +untilDisabled() void
        +untilTextEquals(String text) void
        +untilTextNotEquals(String text) void
        +untilTextContains(String text) void
        +untilValueEquals(String value) void
        +untilValueNotEquals(String value) void
        +untilValueContains(String value) void
        +untilAttributeEquals(String attribute, String value) void
        +untilAttributeNotEquals(String attribute, String value) void
        +untilAttributeContains(String attribute, String value) void
        +untilSelectionStateToBe(boolean selected) void
        +untilChecked() void
        +untilUnchecked() void
        +untilElementsAreExactly(int expectedCount) void
        +untilElementsAreAtLeast(int expectedCount) void
    }

    class RetryAction {
        +retry(Supplier~T~ action, List exceptionsToIgnore)$ T
        +retry(Supplier~T~ action, List exceptionsToIgnore, Supplier~String~ description)$ T
        +retry(Runnable action, List exceptionsToIgnore)$ void
        +retry(Runnable action, List exceptionsToIgnore, Supplier~String~ description)$ void
        +readyCheck(T result)$ T
    }

    class RetryableExceptions {
        <<final>>
        +List COMMON_EXCEPTIONS$
        +List CLICK_EXCEPTIONS$
        +List SEND_KEYS_EXCEPTIONS$
    }

    BaseElement <|.. Element
    WebDriverWait <|-- SeleniumWait
    SeleniumWait <|-- ElementWait
    ElementWait --> BaseElement : element
    Element ..> ElementWait : waits()
    Element ..> RetryAction : every action
    Element ..> RetryableExceptions
    RetryAction ..> SeleniumWait
    ElementWait ..> RetryableExceptions
```

- `Element` implements every `BaseElement` method; each action/accessor runs through
  `RetryAction.retry(...)`, ignoring the matching `RetryableExceptions` list until the timeout.
- `SeleniumWait.timeoutException` records the wait's own expiry so callers can tell it apart from a
  `TimeoutException` thrown by a command inside the condition.

---

## Drivers

Package: `com.sele3.drivers`

```mermaid
classDiagram
    class DriverRunner {
        -DriverContainer driverContainer$
        +initDriver(Configuration config)$ void
        +open()$ void
        +open(String url)$ void
        +close()$ void
        +getDriver()$ Driver
        +getWebDriver()$ WebDriver
        +getRemoteWebDriver()$ RemoteWebDriver
        +getConfig()$ Configuration
        +isDriverAlive()$ boolean
        +isHeadless()$ boolean
        +getCurrentUrl()$ String
        +getPageTitle()$ String
        +takeScreenShot(OutputType~T~ type)$ T
        +executeJS(String script, Object... args)$ Object
        +maximizeWindow()$ void
        +setWindowSize(int width, int height)$ void
        +refreshPage()$ void
        +acceptAlert()$ void
        +closeAlert()$ void
        +sleep(Duration duration)$ void
    }

    class DriverContainer {
        -ThreadLocal~Driver~ threadDriver
        +initialize(Configuration config) void
        +getDriver() Driver
        +hasDriver() boolean
        +quit() void
    }

    class Driver {
        -WebDriver driver
        -Configuration config
        +Driver(Configuration config)
        +createDriver() void
        +getWebDriver() WebDriver
        +getConfig() Configuration
    }

    class DriverFactory {
        +createWebDriver(Configuration config)$ WebDriver
        -findDriverFactory(IPlatform platform)$ IDriverFactory
    }

    class IDriverFactory~T~ {
        <<interface>>
        +getPlatform()* IPlatform
        +getOptions(Configuration config)* T
        +createDriver(T options)* WebDriver
    }

    class ChromeDriverFactory
    class EdgeDriverFactory
    class FirefoxDriverFactory

    class IPlatform {
        <<interface>>
        +name()* String
        +getDefaultConfigFile() String
        +fromString(String name)$ IPlatform
    }

    class Platform {
        <<enumeration>>
        CHROME
        FIREFOX
        EDGE
    }

    DriverRunner --> DriverContainer
    DriverContainer --> Driver : one per thread
    Driver --> Configuration
    Driver ..> DriverFactory : createDriver()
    DriverFactory ..> IDriverFactory : ServiceLoader lookup
    IDriverFactory <|.. ChromeDriverFactory : ChromeOptions
    IDriverFactory <|.. EdgeDriverFactory : EdgeOptions
    IDriverFactory <|.. FirefoxDriverFactory : FirefoxOptions
    IDriverFactory ..> IPlatform : getPlatform()
    IPlatform <|.. Platform
```

- Browsers are discovered with `ServiceLoader` from
  `META-INF/services/com.sele3.drivers.IDriverFactory`. A new browser is a new `IDriverFactory`
  plus an `IPlatform`; no existing class changes.
- When `Configuration.remote` is `true`, `DriverFactory` creates a `RemoteWebDriver` from the
  factory's options instead of calling `createDriver`.
- `IPlatform.getDefaultConfigFile()` returns the lower-case platform name plus `.json`, used by
  `ConfigLoader` to pick the config file.

---

## Configuration

Packages: `com.sele3.configs`, `com.sele3.adapters`

```mermaid
classDiagram
    class ConfigLoader {
        -Gson gson$
        -String CONFIG_SOURCE_PATH$
        -String CLASSPATH_CONFIG_DIR$
        +loadConfig()$ Configuration
        +readConfigFromJsonFile(String jsonFile)$ Configuration
        +readConfigFromClasspath(String resource)$ Configuration
    }

    class Configuration {
        -IPlatform platform
        -boolean headless
        -boolean remote
        -String remoteUrl
        -boolean startMaximized
        -String pageLoadStrategy
        -MutableCapabilities capabilities
        -Duration timeout
        -Duration pollingInterval
        -String baseUrl
        -String windowSize
        +Configuration()
        +Configuration(boolean initial)
        +updateFromSystemProperties() void
    }

    class ConfigKey {
        +String PLATFORM$
        +String HEADLESS$
        +String REMOTE$
        +String REMOTE_URL$
        +String START_MAXIMIZED$
        +String PAGE_LOAD_STRATEGY$
        +String CAPABILITIES$
        +String TIMEOUT$
        +String POLLING_INTERVAL$
        +String BASE_URL$
        +String WINDOW_SIZE$
        +String REPORT_TYPE$
        +String CONFIG_SOURCE_PATH$
        +String CONFIG_FILE_NAME$
    }

    class DurationAdapter
    class CapabilitiesAdapter
    class PlatformAdapter

    ConfigLoader ..> Configuration : creates
    ConfigLoader ..> ConfigKey
    Configuration ..> ConfigKey
    Configuration --> IPlatform : platform
    ConfigLoader ..> DurationAdapter : Gson
    ConfigLoader ..> CapabilitiesAdapter : Gson
    ConfigLoader ..> PlatformAdapter : Gson
```

- `ConfigLoader.loadConfig()` reads `<configSourcePath><file>` from disk, falls back to the
  framework's bundled `configs/<file>` on the classpath, then calls
  `Configuration.updateFromSystemProperties()` so `-D` properties override the file.
- The three adapters let Gson read `Duration` (milliseconds), `MutableCapabilities` (JSON object)
  and `IPlatform` (name) from JSON.

---

## Reports

Package: `com.sele3.reports`

```mermaid
classDiagram
    class ReportRunner {
        -ReportContainer reportContainer$
        +startTest(String name, String description)$ void
        +startTest(IReportType reportType, String name, String description)$ void
        +endTest(IReportStatus status)$ void
        +endTest(IReportStatus status, Throwable error)$ void
        +getReportFactory()$ Optional~IReportFactory~
        +log(IReportStatus status, String message)$ void
        +step(IReportStatus status, String stepName)$ void
        +step(String stepName, Runnable body)$ void
        +logException(Throwable throwable)$ void
        +attachScreenshot(String name)$ void
        +attachScreenshot(String screenshotBase64, String name)$ void
        +attachText(String name, String content)$ void
    }

    class ReportContainer {
        -ThreadLocal~IReportFactory~ threadReportFactory
        +getReportType() IReportType
        +initialize() void
        +initialize(IReportType reportType) void
        +getReportFactory() IReportFactory
        +clear() void
    }

    class IReportFactory {
        <<interface>>
        +getReportType()* IReportType
        +startTest(String name, String description)* void
        +endTest(IReportStatus status)* void
        +endTest(IReportStatus status, Throwable error) void
        +log(IReportStatus status, String message)* void
        +step(IReportStatus status, String stepName)* void
        +step(String stepName, Runnable body)* void
        +logException(Throwable throwable)* void
        +attachScreenshot(String screenshotBase64, String name)* void
        +attachText(String name, String content)* void
        +flush()* void
    }

    class AllureReportFactory
    class ExtentReportFactory {
        -ExtentTest currentTest
    }

    class IReportType {
        <<interface>>
        +name()* String
        +fromString(String name)$ IReportType
        +values()$ List~IReportType~
    }

    class ReportType {
        <<enumeration>>
        ALLURE
        EXTENT
    }

    class IReportStatus {
        <<interface>>
        +isFailureStatus()* boolean
    }

    class ReportStatus {
        <<enumeration>>
        PASS
        FAIL
        SKIP
        INFO
        WARNING
        BROKEN
    }

    ReportRunner --> ReportContainer
    ReportContainer --> IReportFactory : one per thread
    ReportContainer ..> IReportType : reportType property
    IReportFactory <|.. AllureReportFactory
    IReportFactory <|.. ExtentReportFactory
    IReportFactory ..> IReportType : getReportType()
    IReportFactory ..> IReportStatus
    IReportType <|.. ReportType
    IReportStatus <|.. ReportStatus
    AllureReportFactory ..> DriverRunner : screenshots
    ExtentReportFactory ..> DriverRunner : screenshots
```

- Reporters are discovered with `ServiceLoader` from
  `META-INF/services/com.sele3.reports.IReportFactory` and selected with `-DreportType`.
- All `ReportRunner` methods are no-ops when no report type is configured.
- Failure-status steps (`FAIL`, `WARNING`, `BROKEN`) and failing `step(name, body)` calls attach a
  screenshot; a screenshot failure is logged instead of replacing the original error.
