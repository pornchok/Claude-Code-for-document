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
