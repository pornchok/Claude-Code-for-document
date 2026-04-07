# Source Notes: WebdriverIO + Appium Mobile Testing

## WebdriverIO Core

SOURCE: https://webdriver.io/
VERSION: v9 (current)
CONCEPT: Official Definition
QUOTE: "Next-gen browser and mobile automation test framework for Node.js"

SOURCE: https://webdriver.io/docs/gettingstarted/
VERSION: v9
CONCEPT: Quick Start
QUOTE: "The fastest way to configure a new project is running: npm init wdio@latest . This command executes a configuration wizard that helps you to configure your test suite."

SOURCE: https://webdriver.io/docs/gettingstarted/
VERSION: v9
CONCEPT: Run tests
QUOTE: "Execute your test suite with: npx wdio run ./wdio.conf.js"

## WebdriverIO + Appium Service

SOURCE: https://webdriver.io/docs/appium-service/
VERSION: v9
CONCEPT: Install Appium Service
QUOTE: "Install via npm: npm install @wdio/appium-service --save-dev"

SOURCE: https://webdriver.io/docs/appium-service/
VERSION: v9
CONCEPT: Appium Service Config
QUOTE: "Add to your wdio.conf.js: export const config = { port: 4723, services: ['appium'] };"

## Capabilities for Mobile

SOURCE: https://webdriver.io/docs/appium/
VERSION: v9
CONCEPT: Platforms supported
QUOTE: "You can test: Mobile apps on iOS, Android, or Tizen; Desktop applications on macOS or Windows; TV apps for Roku, tvOS, Android TV, and Samsung"

## Selectors

SOURCE: https://webdriver.io/docs/selectors/
VERSION: v9
CONCEPT: Accessibility ID cross-platform
QUOTE: "Accessibility ID: The accessibility id strategy works across platforms—for iOS it maps to accessibility identifiers, while for Android it corresponds to content-description for the element."

SOURCE: https://webdriver.io/docs/selectors/
VERSION: v9
CONCEPT: Android UiAutomator selector
QUOTE: "Android: Uses UiAutomator selectors like android=new UiSelector().text('Cancel')"

SOURCE: https://webdriver.io/docs/selectors/
VERSION: v9
CONCEPT: iOS Predicate String
QUOTE: "iOS: Supports UIAutomation, XCUITest predicate strings, and class chains such as ios predicate string:type == 'XCUIElementTypeSwitch'"

## Mobile Commands

SOURCE: https://webdriver.io/docs/api/mobile/
VERSION: v9
CONCEPT: Mobile Commands Abstraction
QUOTE: "WebdriverIO abstracts away complex Appium APIs to enable concise, intuitive, and platform-agnostic test scripts. For example, instead of manually constructing action chains for a long press, you can simply call .longPress()."

SOURCE: https://webdriver.io/docs/api/mobile/
VERSION: v9
CONCEPT: Cross-platform
QUOTE: "Commands work on both Android and iOS without conditional logic."

SOURCE: https://webdriver.io/docs/api/mobile/
VERSION: v9
CONCEPT: Hybrid App context switching
QUOTE: "WebdriverIO includes specialized commands for hybrid apps: getContext/getContexts - Retrieves current or available contexts; switchContext - Switches between webviews"

## Page Objects

SOURCE: https://webdriver.io/docs/pageobjects/
VERSION: v9
CONCEPT: Goal
QUOTE: "The goal of using page objects is to abstract any page information away from the actual tests."

SOURCE: https://webdriver.io/docs/pageobjects/
VERSION: v9
CONCEPT: Lazy Loading (getter functions)
QUOTE: "Selectors defined as getter functions are evaluated when accessed, ensuring elements are requested immediately before use rather than during object initialization."

SOURCE: https://webdriver.io/docs/pageobjects/
VERSION: v9
CONCEPT: Structure
QUOTE: "A Page Object Pattern implementation consists of: Base Page Class: A foundational class containing shared methods and selectors; Page-Specific Classes: Inherited classes representing individual pages"

## Mocha Framework

SOURCE: https://webdriver.io/docs/frameworks/
VERSION: v9
CONCEPT: Framework support
QUOTE: "WebdriverIO's test runner supports multiple testing frameworks including Mocha, Jasmine, and Cucumber.js."

SOURCE: https://webdriver.io/docs/frameworks/
VERSION: v9
CONCEPT: Install Mocha adapter
QUOTE: "To use Mocha, install the adapter: npm install @wdio/mocha-framework --save-dev"

SOURCE: https://webdriver.io/docs/frameworks/
VERSION: v9
CONCEPT: Mocha test structure
QUOTE: "A basic Mocha test looks like this: describe('my awesome website', () => { it('should do some assertions', async () => { await browser.url('https://webdriver.io') }) })"

## WebdriverIO v9 Release

SOURCE: https://webdriver.io/blog/2024/08/15/webdriverio-v9-release/
VERSION: v9 (August 15, 2024)
CONCEPT: v9 Release highlights
QUOTE: "WebdriverIO v9, released August 15, 2024, represents a major milestone leveraging the new WebDriver Bidi protocol for enhanced browser automation capabilities."
