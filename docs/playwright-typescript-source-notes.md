# Playwright TypeScript Course — Source Notes

ไฟล์นี้บันทึก QUOTE จาก playwright.dev สำหรับ cross-check accuracy
ลบไฟล์นี้ได้หลังจาก Quality Review ผ่านทั้ง 18 บทแล้วเท่านั้น

---

## System Requirements

SOURCE: https://playwright.dev/docs/intro
VERSION: 2026-05-30
CONCEPT: Node.js and npm requirements
QUOTE: "Node.js: latest 20.x, 22.x or 24.x"

---

## TypeScript Support

SOURCE: https://playwright.dev/docs/test-typescript
VERSION: 2026-05-30
CONCEPT: TypeScript native support
QUOTE: "You just write tests in TypeScript, and Playwright will read them, transform to JavaScript and run."

SOURCE: https://playwright.dev/docs/test-typescript
VERSION: 2026-05-30
CONCEPT: tsconfig.json detection
QUOTE: "Playwright will automatically find and use the closest tsconfig.json when discovering tests."

SOURCE: https://playwright.dev/docs/test-typescript
VERSION: 2026-05-30
CONCEPT: Supported tsconfig compiler options
QUOTE: "Only the following options are supported: allowJs, baseUrl, paths, and references"

---

## Installation

SOURCE: https://playwright.dev/docs/intro
VERSION: 2026-05-30
CONCEPT: Installation command
QUOTE: "npm init playwright@latest"

---

## Installation Methods

SOURCE: https://playwright.dev/docs/intro
VERSION: 2026-05-30
CONCEPT: Supported package managers
QUOTE: "You can use npm, yarn, or pnpm to install Playwright."

---

## Ch01: Why Playwright?

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-30
CONCEPT: Auto-waiting / Actionability checks
QUOTE: "Playwright performs a range of actionability checks on the elements before making actions to ensure these actions behave as expected."

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-30
CONCEPT: Visible check definition
QUOTE: "Element is considered visible when it has non-empty bounding box and does not have `visibility:hidden` computed style."

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-30
CONCEPT: Stable check definition
QUOTE: "Element is considered stable when it has maintained the same bounding box for at least two consecutive animation frames."

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-30
CONCEPT: Enabled check definition
QUOTE: "Element is considered enabled when it is not disabled."

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-30
CONCEPT: Editable check definition
QUOTE: "Element is considered editable when it is enabled and is not readonly."

SOURCE: https://playwright.dev/docs/best-practices
VERSION: 2026-05-30
CONCEPT: Locators auto-waiting
QUOTE: "Locators come with auto waiting and retry-ability. Auto waiting means that Playwright performs a range of actionability checks on the elements, such as ensuring the element is visible and enabled before it performs the click."

SOURCE: https://playwright.dev/docs/best-practices
VERSION: 2026-05-30
CONCEPT: Web-first assertions wait
QUOTE: "By using web first assertions Playwright will wait until the expected condition is met."

SOURCE: https://playwright.dev/docs/best-practices
VERSION: 2026-05-30
CONCEPT: isVisible() does not wait
QUOTE: "When using assertions such as `isVisible()` the test won't wait a single second, it will just check the locator is there and return immediately."

SOURCE: https://playwright.dev/docs/intro
VERSION: 2026-05-30
CONCEPT: Cross-browser support
QUOTE: "Playwright supports Chromium, WebKit and Firefox on Windows, Linux and macOS, locally or in CI, headless or headed, with native mobile emulation for Chrome (Android) and Mobile Safari."

SOURCE: https://playwright.dev/docs/intro
VERSION: 2026-05-30
CONCEPT: What Playwright Test is
QUOTE: "Playwright Test is an end-to-end test framework for modern web apps. It bundles test runner, assertions, isolation, parallelization and rich tooling."

---

## Ch02: Setup + TypeScript Essentials

SOURCE: https://playwright.dev/docs/test-typescript
VERSION: 2026-05-30
CONCEPT: TypeScript native support — no separate compilation step
QUOTE: "You just write tests in TypeScript, and Playwright will read them, transform to JavaScript and run."

SOURCE: https://playwright.dev/docs/test-typescript
VERSION: 2026-05-30
CONCEPT: Supported tsconfig compiler options
QUOTE: "Playwright only supports the following tsconfig options: allowJs, baseUrl, paths and references."

SOURCE: https://playwright.dev/docs/test-typescript
VERSION: 2026-05-30
CONCEPT: Separate tsconfig for tests recommended
QUOTE: "We recommend setting up a separate tsconfig.json in the tests directory so that you can change some preferences specifically for the tests."

SOURCE: https://playwright.dev/docs/test-typescript
VERSION: 2026-05-30
CONCEPT: tsc --noEmit for CI type checking
QUOTE: "For example on GitHub actions: npx tsc -p tsconfig.json --noEmit"

SOURCE: https://playwright.dev/docs/test-typescript
VERSION: 2026-05-30
CONCEPT: tsc watch mode for local development
QUOTE: "For local development, you can run tsc in watch mode like this: npx tsc -p tsconfig.json --noEmit -w"

SOURCE: https://playwright.dev/docs/intro
VERSION: 2026-05-30
CONCEPT: Installation command description
QUOTE: "The command below either initializes a new project or adds Playwright to an existing one."

SOURCE: https://playwright.dev/docs/intro
VERSION: 2026-05-30
CONCEPT: Interactive prompts during npm init playwright@latest
QUOTE: "When prompted, choose / confirm: TypeScript or JavaScript (default: TypeScript), Tests folder name (default: tests, or e2e if tests already exists), Add a GitHub Actions workflow (recommended for CI), Install Playwright browsers (default: yes)"

SOURCE: https://playwright.dev/docs/intro
VERSION: 2026-05-30
CONCEPT: File scaffold created by installer
QUOTE: "Playwright downloads required browser binaries and creates the scaffold below. playwright.config.ts, package.json, package-lock.json, tests/example.spec.ts"

---

## Ch03: Architecture — Browser, BrowserContext, Page

SOURCE: https://playwright.dev/docs/browser-contexts
VERSION: 2026-05-30
CONCEPT: BrowserContext definition and isolation
QUOTE: "BrowserContexts which are equivalent to incognito-like profiles. They are fast and cheap to create and are completely isolated, even when running in a single browser."

SOURCE: https://playwright.dev/docs/browser-contexts
VERSION: 2026-05-30
CONCEPT: Default isolation per test
QUOTE: "each test has its own local storage, session storage, cookies etc."

SOURCE: https://playwright.dev/docs/browser-contexts
VERSION: 2026-05-30
CONCEPT: Playwright creates context per test
QUOTE: "Playwright creates a context for each test, and provides a default Page in that context."

SOURCE: https://playwright.dev/docs/browser-contexts
VERSION: 2026-05-30
CONCEPT: No failure carry-over between tests
QUOTE: "No failure carry-over. If one test fails it doesn't affect the other test."

SOURCE: https://playwright.dev/docs/browser-contexts
VERSION: 2026-05-30
CONCEPT: Multiple contexts for multi-user scenarios
QUOTE: "Playwright can create multiple browser contexts within a single scenario. This is useful when you want to test for multi-user functionality, like a chat."

SOURCE: https://playwright.dev/docs/browser-contexts
VERSION: 2026-05-30
CONCEPT: Context lifecycle per test run
QUOTE: "Running the test creates a new browser context each time."

SOURCE: https://playwright.dev/docs/pages
VERSION: 2026-05-30
CONCEPT: Page definition
QUOTE: "A Page refers to a single tab or a popup window within a browser context."

SOURCE: https://playwright.dev/docs/pages
VERSION: 2026-05-30
CONCEPT: Multiple pages per context
QUOTE: "Each BrowserContext can have multiple pages."

SOURCE: https://playwright.dev/docs/pages
VERSION: 2026-05-30
CONCEPT: Pages respect context-level settings
QUOTE: "Pages inside a context respect context-level emulation, like viewport sizes, custom network routes or browser locale."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-30
CONCEPT: page fixture — isolated per test
QUOTE: "Isolated page for this test run."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-30
CONCEPT: context fixture — isolated per test
QUOTE: "Isolated context for this test run. The `page` fixture belongs to this context as well."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-30
CONCEPT: Browser fixture — shared across tests (worker-scoped)
QUOTE: "Browsers are shared across tests to optimize resources."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-30
CONCEPT: Fixture scope and teardown
QUOTE: "Test-scoped fixtures are torn down after each test, while worker-scoped fixtures are only torn down when the worker process executing tests is torn down."

---

## Ch04: Locators — Accessibility-First

SOURCE: https://playwright.dev/docs/locators
VERSION: 2026-05-31
CONCEPT: Recommended locators list (official list)
QUOTE: "These are the recommended built-in locators. page.getByRole() to locate by explicit and implicit accessibility attributes. page.getByText() to locate by text content. page.getByLabel() to locate a form control by associated label's text. page.getByPlaceholder() to locate an input by placeholder. page.getByAltText() to locate an element, usually image, by its text alternative. page.getByTitle() to locate an element by its title attribute. page.getByTestId() to locate an element based on its data-testid attribute."

SOURCE: https://playwright.dev/docs/locators
VERSION: 2026-05-31
CONCEPT: Priority recommendation
QUOTE: "we recommend prioritizing user-facing attributes and explicit contracts such as page.getByRole()"

SOURCE: https://playwright.dev/docs/locators
VERSION: 2026-05-31
CONCEPT: Strict mode definition
QUOTE: "Locators are strict. This means that all operations on locators that imply some target DOM element will throw an exception if more than one element matches."

SOURCE: https://playwright.dev/docs/locators
VERSION: 2026-05-31
CONCEPT: filter({ visible: true }) — confirmed available
QUOTE: "This will only find a second button, because it is visible, and then click it: await page.locator('button').filter({ visible: true }).click();"

SOURCE: https://playwright.dev/docs/locators
VERSION: 2026-05-31
CONCEPT: Filtering with hasText and has
QUOTE: "filter({ hasText: 'text' }), filter({ hasNotText: 'text' }), filter({ has: locator }), filter({ hasNot: locator })"

SOURCE: https://playwright.dev/docs/best-practices
VERSION: 2026-05-31
CONCEPT: Prefer user-facing attributes over CSS/XPath
QUOTE: "Prefer user-facing attributes to XPath or CSS selectors" and "Your DOM can easily change so having your tests depend on your DOM structure can lead to failing tests."

SOURCE: https://playwright.dev/docs/best-practices
VERSION: 2026-05-31
CONCEPT: CSS class-based locators break on refactor
QUOTE: "Should the designer change something then the class might change, thus breaking your test."

SOURCE: https://playwright.dev/docs/other-locators
VERSION: 2026-05-31
CONCEPT: Legacy text locator (deprecated)
QUOTE: "String selectors starting and ending with a quote...are assumed to be a legacy text locators." (deprecated — recommends "modern text locator instead")

SOURCE: https://playwright.dev/docs/other-locators
VERSION: 2026-05-31
CONCEPT: CSS :visible pseudo-class
QUOTE: ":visible — only matches visible buttons"

---

## Ch04: Locator Priority Order — Verified Fix (2026-05-31)

SOURCE: https://playwright.dev/docs/locators
VERSION: 2026-05-31
CONCEPT: Locator Priority Order — Verified Fix
QUOTE: "These are the recommended built-in locators. page.getByRole() to locate by explicit and implicit accessibility attributes. page.getByText() to locate by text content. page.getByLabel() to locate a form control by associated label's text. page.getByPlaceholder() to locate an input by placeholder. page.getByAltText() to locate an element, usually image, by its text alternative. page.getByTitle() to locate an element by its title attribute. page.getByTestId() to locate an element based on its data-testid attribute."

Note: Correct order is getByRole → getByText → getByLabel → getByPlaceholder → getByAltText → getByTitle → getByTestId

---

## Ch05: Actions & Assertions

SOURCE: https://playwright.dev/docs/input
VERSION: 2026-05-31
CONCEPT: fill() — replace input content immediately
QUOTE: "the easiest way to fill out the form fields. It focuses the element and triggers an `input` event with the entered text. It works for `<input>`, `<textarea>` and `[contenteditable]` elements."

SOURCE: https://playwright.dev/docs/input
VERSION: 2026-05-31
CONCEPT: pressSequentially() — keystroke-by-keystroke typing
QUOTE: "character by character, as if it was a user with a real keyboard"

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-31
CONCEPT: Visible check definition
QUOTE: "Element is considered visible when it has non-empty bounding box and does not have `visibility:hidden` computed style."

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-31
CONCEPT: Stable check definition
QUOTE: "Element is considered stable when it has maintained the same bounding box for at least two consecutive animation frames."

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-31
CONCEPT: Receives Events check definition
QUOTE: "Element is considered receiving pointer events when it is the hit target of the pointer event at the action point."

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-31
CONCEPT: Enabled check definition
QUOTE: "Element is considered enabled when it is not disabled."

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-31
CONCEPT: Editable check definition
QUOTE: "Element is considered editable when it is [enabled] and is not readonly."

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-31
CONCEPT: Actions with all 5 checks
QUOTE: "locator.click(), locator.check(), locator.dblclick(), locator.setChecked(), locator.tap(), locator.uncheck()" require all checks

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-31
CONCEPT: fill() and clear() actionability checks
QUOTE: "locator.fill(), locator.clear()" — checks: Visible + Enabled + Editable

SOURCE: https://playwright.dev/docs/actionability
VERSION: 2026-05-31
CONCEPT: press() and pressSequentially() — no actionability checks
QUOTE: "locator.press(), locator.pressSequentially()" — listed under "No Checks" category

SOURCE: https://playwright.dev/docs/navigations
VERSION: 2026-05-31
CONCEPT: page.goto() waits for load event
QUOTE: "The code above loads the page and waits for the web page to fire the load event."

SOURCE: https://playwright.dev/docs/navigations
VERSION: 2026-05-31
CONCEPT: Auto-waiting principle
QUOTE: "It will automatically wait for the target elements to become actionable."

SOURCE: https://playwright.dev/docs/navigations
VERSION: 2026-05-31
CONCEPT: Playwright speed and actionability
QUOTE: "Playwright operates as a very fast user - the moment it sees the button, it clicks it. In the general case, you don't need to worry about whether all the resources loaded."

SOURCE: https://playwright.dev/docs/test-assertions
VERSION: 2026-05-31
CONCEPT: Web-first assertions retry behavior
QUOTE: "Playwright will be re-testing the element with the test id of `status` until the fetched element has the `\"Submitted\"` text."

SOURCE: https://playwright.dev/docs/test-assertions
VERSION: 2026-05-31
CONCEPT: Soft assertions with expect.soft()
QUOTE: "Using expect.soft() allows assertions to fail without terminating test execution, enabling multiple validations per test while still marking failures."

SOURCE: https://playwright.dev/docs/test-assertions
VERSION: 2026-05-31
CONCEPT: Custom matchers with expect.extend()
QUOTE: "The expect.extend() method enables developers to create domain-specific assertions tailored to application requirements."

SOURCE: https://playwright.dev/docs/test-assertions
VERSION: 2026-05-31
CONCEPT: toHaveRole() — ARIA role assertion
QUOTE: "toHaveRole() - validates specific ARIA role"

SOURCE: https://playwright.dev/docs/test-assertions
VERSION: 2026-05-31
CONCEPT: toMatchAriaSnapshot() — aria snapshot matching
QUOTE: "toMatchAriaSnapshot() - Element matches the Aria snapshot"
Previous version of ch04 had getByLabel at #2 and getByText at #4 — this was wrong. Fixed 2026-05-31.

---

## Ch06: Debugging

SOURCE: https://playwright.dev/docs/debug
VERSION: 2026-05-31
CONCEPT: Inspector — opening with --debug flag
QUOTE: "Run with the --debug flag to open the GUI debugging tool: npx playwright test --debug"

SOURCE: https://playwright.dev/docs/debug
VERSION: 2026-05-31
CONCEPT: page.pause() as code breakpoint
QUOTE: "Insert breakpoints directly in code: await page.pause(); This pauses execution at that point without stepping through prior actions."

SOURCE: https://playwright.dev/docs/debug
VERSION: 2026-05-31
CONCEPT: PWDEBUG=console for DevTools integration
QUOTE: "Set PWDEBUG=console environment variable to access a playwright object in DevTools console"

SOURCE: https://playwright.dev/docs/debug
VERSION: 2026-05-31
CONCEPT: headless: false and slowMo for headed mode
QUOTE: "Launch browsers visually by setting headless: false in configuration or using slowMo to slow execution by milliseconds per operation."

SOURCE: https://playwright.dev/docs/trace-viewer
VERSION: 2026-05-31
CONCEPT: Trace config options
QUOTE: "'on-first-retry' - Record only on first retry attempt; 'on-all-retries' - Capture traces for all retries; 'off' - Disable tracing; 'on' - Record every test (performance-heavy); 'retain-on-failure' - Trace failed tests only"

SOURCE: https://playwright.dev/docs/trace-viewer
VERSION: 2026-05-31
CONCEPT: Opening trace from command line
QUOTE: "npx playwright show-trace path/to/trace.zip"

SOURCE: https://playwright.dev/docs/trace-viewer
VERSION: 2026-05-31
CONCEPT: Actions tab shows locator and duration
QUOTE: "what locator was used for every action and how long each one took"

SOURCE: https://playwright.dev/docs/trace-viewer
VERSION: 2026-05-31
CONCEPT: DOM snapshots show exact click position
QUOTE: "the exact click position"

SOURCE: https://playwright.dev/docs/codegen
VERSION: 2026-05-31
CONCEPT: Codegen generates tests from browser interactions
QUOTE: "Playwright comes with the ability to generate tests for you as you perform actions in the browser."

SOURCE: https://playwright.dev/docs/codegen
VERSION: 2026-05-31
CONCEPT: Codegen locator priority
QUOTE: "prioritizes role, text and test id locators"

SOURCE: https://playwright.dev/docs/test-ui-mode
VERSION: 2026-05-31
CONCEPT: UI Mode — core purpose
QUOTE: "UI Mode lets you explore, run, and debug tests with a time travel experience complete with a watch mode."

SOURCE: https://playwright.dev/docs/test-ui-mode
VERSION: 2026-05-31
CONCEPT: UI Mode watch mode activation
QUOTE: "Eye icons activate automatic re-runs when test code changes"

---

## Ch07: Fixtures

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: What fixtures are and why use them
QUOTE: "Test fixtures are used to establish the environment for each test, giving the test everything it needs and nothing else. Test fixtures are isolated between tests."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: Fixtures are on-demand (lazy initialization)
QUOTE: "Fixtures are on-demand - you can define as many fixtures as you'd like, and Playwright Test will setup only the ones needed by your test."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: Fixtures are composable
QUOTE: "Fixtures are composable - they can depend on each other to provide complex behaviors."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: Built-in page fixture
QUOTE: "Isolated page for this test run."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: Built-in context fixture
QUOTE: "Isolated context for this test run."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: Built-in browser fixture
QUOTE: "Browsers are shared across tests to optimize resources."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: Built-in request fixture
QUOTE: "Isolated APIRequestContext instance for this test run."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: Worker-scoped fixture definition
QUOTE: "Playwright Test uses worker processes to run test files. Similar to how test fixtures are set up for individual test runs, worker fixtures are set up for each worker process."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: Worker-scoped tuple syntax
QUOTE: "Note the tuple-like syntax for the worker fixture - we have to pass `{scope: 'worker'}` so that test runner sets this fixture up once per worker."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: Worker process reuse
QUOTE: "Playwright Test will reuse the worker process for as many test files as it can, provided their worker fixtures match and hence environments are identical."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: Automatic fixtures definition
QUOTE: "Automatic fixtures are set up for each test/worker, even when the test does not list them directly. To create an automatic fixture, use the tuple syntax and pass `{ auto: true }`."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: Fixture dependency ordering
QUOTE: "When fixture A depends on fixture B: B is always set up before A and torn down after A."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: use() callback and teardown
QUOTE: "Each fixture has a setup and teardown phase before and after the `await use()` call in the fixture. Setup is executed before the test/hook requiring it is run, and teardown is executed when the fixture is no longer being used."

SOURCE: https://playwright.dev/docs/test-fixtures
VERSION: 2026-05-31
CONCEPT: mergeTests() for combining fixtures
QUOTE: (combining fixtures from multiple files) "export const test = mergeTests(dbTest, a11yTest);"

---

## Ch08: Page Object Model

SOURCE: https://playwright.dev/docs/pom
VERSION: 2026-05-31
CONCEPT: POM definition
QUOTE: "A page object represents a part of your web application."

SOURCE: https://playwright.dev/docs/pom
VERSION: 2026-05-31
CONCEPT: POM primary benefits
QUOTE: "Page objects simplify authoring by creating a higher-level API which suits your application and simplify maintenance by capturing element selectors in one place and create reusable code to avoid repetition."

SOURCE: https://playwright.dev/docs/pom
VERSION: 2026-05-31
CONCEPT: POM implementation approach
QUOTE: "We will create a PlaywrightDevPage helper class to encapsulate common operations on the playwright.dev page."

SOURCE: https://playwright.dev/docs/pom
VERSION: 2026-05-31
CONCEPT: POM usage pattern
QUOTE: "const playwrightDev = new PlaywrightDevPage(page); await playwrightDev.goto(); await playwrightDev.getStarted();"

SOURCE: https://playwright.dev/docs/pom
VERSION: 2026-05-31
CONCEPT: Locator properties in Page Object
QUOTE: "constructor(page: Page) { this.page = page; this.getStartedLink = page.locator('a', { hasText: 'Get started' });"

---

---

## Ch09: Test Organization

SOURCE: https://playwright.dev/docs/test-annotations
VERSION: 2026-05-31
CONCEPT: test.skip behavior
QUOTE: "Skip a test. Playwright will not run the test past the test.skip() call."

SOURCE: https://playwright.dev/docs/test-annotations
VERSION: 2026-05-31
CONCEPT: test.fail behavior
QUOTE: "marks the test as failing. Playwright will run this test and ensure it does indeed fail."

SOURCE: https://playwright.dev/docs/test-annotations
VERSION: 2026-05-31
CONCEPT: test.fixme behavior
QUOTE: "marks the test as failing. Playwright will not run this test, as opposed to the fail annotation."

SOURCE: https://playwright.dev/docs/test-annotations
VERSION: 2026-05-31
CONCEPT: test.slow behavior
QUOTE: "marks the test as slow and triples the test timeout."

SOURCE: https://playwright.dev/docs/test-annotations
VERSION: 2026-05-31
CONCEPT: Conditional annotations
QUOTE: "Built-in annotations can be conditional, in which case they apply when the condition is truthy, and may depend on test fixtures."

SOURCE: https://playwright.dev/docs/test-annotations
VERSION: 2026-05-31
CONCEPT: Runtime annotations via testInfo
QUOTE: "During test execution, you can dynamically add annotations using test.info().annotations.push() with custom type and description properties."

SOURCE: https://playwright.dev/docs/api/class-test
VERSION: 2026-05-31
CONCEPT: test.skip API signature
QUOTE: "Marks a test as 'should fail'. Playwright runs this test and ensures that it is actually failing."

SOURCE: https://playwright.dev/docs/api/class-test
VERSION: 2026-05-31
CONCEPT: test.fail API signature
QUOTE: "Marks a test as 'should fail'. Playwright runs this test and ensures that it is actually failing."

SOURCE: https://playwright.dev/docs/api/class-test
VERSION: 2026-05-31
CONCEPT: test.fixme API signature
QUOTE: "Mark a test as 'fixme', with the intention to fix it. Playwright will not run the test past the test.fixme() call."

SOURCE: https://playwright.dev/docs/api/class-test
VERSION: 2026-05-31
CONCEPT: test.slow API signature
QUOTE: "Marks a test as 'slow'. Slow test will be given triple the default timeout."

SOURCE: https://playwright.dev/docs/api/class-test
VERSION: 2026-05-31
CONCEPT: test.describe.configure modes
QUOTE: "mode: 'default' | 'parallel' | 'serial' — Running tests serially, retrying from the start. If one of the serial tests fails, all subsequent tests are skipped."

SOURCE: https://playwright.dev/docs/api/class-test
VERSION: 2026-05-31
CONCEPT: test.step signature and return value
QUOTE: "The method returns the value returned by the step callback."

SOURCE: https://playwright.dev/docs/api/class-test
VERSION: 2026-05-31
CONCEPT: test.step options
QUOTE: "timeout [number] — The maximum time, in milliseconds, allowed for the step to complete."

SOURCE: https://playwright.dev/docs/api/class-test
VERSION: 2026-05-31
CONCEPT: Tags inline syntax
QUOTE: "Tests can include tags in the test title. Note that each tag must start with @ symbol."

SOURCE: https://playwright.dev/docs/api/class-test
VERSION: 2026-05-31
CONCEPT: Tags object syntax and reporting
QUOTE: "Test tags are displayed in the test report, and are available to a custom reporter via TestCase.tags property."

SOURCE: https://playwright.dev/docs/test-parameterize
VERSION: 2026-05-31
CONCEPT: Parameterized tests with forEach
QUOTE: "Uses template literals for dynamic test names — Test names must remain unique across iterations"

---

## Ch10: Configuration & Projects

SOURCE: https://playwright.dev/docs/test-configuration
VERSION: 2026-05-31
CONCEPT: defineConfig() — fullyParallel option
QUOTE: "Have all tests in all files run in parallel"

SOURCE: https://playwright.dev/docs/test-configuration
VERSION: 2026-05-31
CONCEPT: defineConfig() — forbidOnly option
QUOTE: "Exit with error if tests marked as test.only exist; useful for CI"

SOURCE: https://playwright.dev/docs/test-configuration
VERSION: 2026-05-31
CONCEPT: defineConfig() — retries recommended pattern
QUOTE: "retries: process.env.CI ? 2 : 0"

SOURCE: https://playwright.dev/docs/test-timeouts
VERSION: 2026-05-31
CONCEPT: Test timeout — default value
QUOTE: "Timeout for each test" — default 30,000 ms

SOURCE: https://playwright.dev/docs/test-timeouts
VERSION: 2026-05-31
CONCEPT: Expect timeout — default value
QUOTE: "5 seconds for web-first assertions" (expect.timeout default)

SOURCE: https://playwright.dev/docs/test-timeouts
VERSION: 2026-05-31
CONCEPT: Action timeout — no default
QUOTE: "actionTimeout" in use block — "No default" (no timeout by default)

SOURCE: https://playwright.dev/docs/test-timeouts
VERSION: 2026-05-31
CONCEPT: Navigation timeout — no default
QUOTE: "navigationTimeout" in use block — "No default" (no timeout by default)

SOURCE: https://playwright.dev/docs/test-timeouts
VERSION: 2026-05-31
CONCEPT: Global timeout — no default
QUOTE: "Timeout for the whole test run" — globalTimeout — "No default"

SOURCE: https://playwright.dev/docs/test-timeouts
VERSION: 2026-05-31
CONCEPT: beforeAll/afterAll timeout — 30,000 ms default
QUOTE: "beforeAll/afterAll timeout" — default 30,000 ms, set via test.setTimeout() within hook

SOURCE: https://playwright.dev/docs/test-webserver
VERSION: 2026-05-31
CONCEPT: webServer reuseExistingServer behavior
QUOTE: "reuseExistingServer — Whether to reuse existing server on port/url" — default false

SOURCE: https://playwright.dev/docs/test-webserver
VERSION: 2026-05-31
CONCEPT: webServer timeout — default
QUOTE: "How long to wait for the process to start up and be available in milliseconds" — default 60000

SOURCE: https://playwright.dev/docs/test-webserver
VERSION: 2026-05-31
CONCEPT: webServer port option deprecated
QUOTE: "port — Deprecated; use url instead"

SOURCE: https://playwright.dev/docs/test-projects
VERSION: 2026-05-31
CONCEPT: Project dependencies — setup project runs first
QUOTE: "Setup projects execute first, and dependent projects only run if dependencies pass."

SOURCE: https://playwright.dev/docs/test-global-setup-teardown
VERSION: 2026-05-31
CONCEPT: Project Dependencies vs globalSetup — modern approach recommended
QUOTE: "the recommended approach, as it integrates better with the Playwright test runner: your HTML report will include the global setup, traces will be recorded, and fixtures can be used."

SOURCE: https://playwright.dev/docs/api/class-testconfig
VERSION: 2026-05-31
CONCEPT: tsconfig property in TestConfig — exact property name
QUOTE: "Path to a single tsconfig applicable to all imported files." — property name: tsconfig (added v1.49)

SOURCE: https://playwright.dev/docs/api/class-testconfig
VERSION: 2026-05-31
CONCEPT: failOnFlakyTests — confirmed to exist in Playwright v1.52+
QUOTE: "failOnFlakyTests: !!process.env.CI" — exits with error if any test is marked flaky (added v1.52, NOT an invented property)

