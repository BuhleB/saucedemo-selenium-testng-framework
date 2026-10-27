# SauceDemo Automation Framework

UI test automation framework for the public demo shop [saucedemo.com](https://www.saucedemo.com), built with **Java, Selenium WebDriver, TestNG and Maven** using the Page Object Model. It runs on Chrome, Firefox and Edge and produces an HTML report (ExtentReports) with a screenshot for every failed test.

> **Context:** this started as a team project on the CAPACITI software testing programme (test plan, manual test cases and defect reporting were team deliverables). The automation framework in this repository is my own work.

## Tech stack

| Area | Tool |
| --- | --- |
| Language / build | Java 11, Maven |
| Browser automation | Selenium WebDriver 4.51.0 (Selenium Manager handles the browser drivers) |
| Test runner | TestNG 7.8.0 |
| Reporting | ExtentReports 4.1.7 (Spark reporter) |

## Project structure

```
src/main/java/com/saucedemo
  base/        BaseTest (browser setup/teardown, wait helpers), BasePage (explicit waits)
  pages/       LoginPage, ProductsPage, CartPage, CheckoutPage (Page Objects)
  utils/       DriverManager (one driver per thread), ExtentReportManager
  listeners/   TestListener (results + screenshot on failure)
src/main/resources/META-INF/services/
               org.testng.ITestNGListener  (registers TestListener for every suite)
src/test/java/com/saucedemo/tests
               LoginTests, InventoryTests, CheckoutTests, DataDrivenTests, SmokeTests, CrossBrowserTests
src/test/resources
               testng-full-regression.xml, testng-smoke-only.xml, testng-sequential-browsers.xml
```

## What is covered

| Area | Scenarios |
| --- | --- |
| Login | valid login; locked-out user; wrong password; empty username; empty password; `performance_glitch_user` (slow login); `problem_user`; masked password field; login button state; repeated failed attempts |
| Inventory | 6 products shown; product name/price/description present; add to cart; add and remove; add several items; cart survives a refresh; sort by price (low to high) and name (Z to A); logout |
| Checkout | full purchase; each required field left empty (first name, last name, postal code); several items; cancel; special characters in the form |
| Data-driven login | one login scenario per SauceDemo user type (standard, locked out, problem, performance glitch, error, visual) |
| Cross-browser | invalid login, valid login, complete purchase, logout, add/remove cart items on Chrome, Firefox and Edge |

## Test suites

| Suite file | Runs | Browser | Test executions |
| --- | --- | --- | --- |
| `testng-full-regression.xml` (default) | `LoginTests` (10), `InventoryTests` (9), `CheckoutTests` (7), `DataDrivenTests` (6 rows) | Chrome | 32 |
| `testng-smoke-only.xml` | `SmokeTests` (7) | Chrome | 7 |
| `testng-sequential-browsers.xml` | `CrossBrowserTests` (5) once per browser, one browser after the other | Chrome, Firefox, Edge | 15 |

## How to run

**Prerequisites:** JDK 11 or newer, Maven 3.6+, Google Chrome, and an internet connection. Firefox and Edge must also be installed to run the cross-browser suite. The browser drivers are downloaded automatically by Selenium Manager.

```bash
# default: full regression on Chrome
mvn clean test

# smoke suite
mvn clean test -DsuiteXmlFile=src/test/resources/testng-smoke-only.xml

# same journeys on Chrome, Firefox and Edge
mvn clean test -DsuiteXmlFile=src/test/resources/testng-sequential-browsers.xml

# headless (no browser window), works with any suite
mvn clean test -DsuiteXmlFile=src/test/resources/testng-smoke-only.xml -Dheadless=true

# open the report in your browser when the run finishes
mvn clean test -DopenReport=true
```

On Windows, `run.bat [suite-file]` runs the same command.

The browser is set by the `browser` parameter inside each suite XML file (`chrome`, `firefox` or `edge`). To run another browser, change that value or add a suite file.

## Reports

Every run writes an HTML report to `test-output/ExtentReports/TestReport_<timestamp>.html`. A failed test shows the error and a screenshot taken at the moment of failure (saved in `test-output/ExtentReports/screenshots/`). Open the HTML file in any browser. An example report is kept in `sample-report/`.

## Design notes

- **Page Object Model:** locators and page actions live in `pages/`; test classes contain only the scenario and its assertions.
- **One browser per test, one driver per thread:** `DriverManager` keeps the driver in a `ThreadLocal`, so suites can be run in parallel without tests sharing a browser.
- **Reporting in one place:** `TestListener` (registered through the TestNG service file) creates the report entries and attaches screenshots. Test classes have no reporting code.
- **Waits:** explicit waits (`WebDriverWait`, `ExpectedConditions`) for page changes, plus a short 5-second implicit wait for element lookup. No fixed sleeps.

## Known limitations

- The tests run against the live demo site, so a slow network or a change on saucedemo.com can cause failures.
- `error_user`, `problem_user` and `visual_user` are only checked at login level. Their known defects are not asserted.
- The suites do not run in parallel by default.
- UI tests only; there are no API tests.

## Latest results

_To be filled in after the first full run: date, browser versions, and passed/failed counts for each suite._
