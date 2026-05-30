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
