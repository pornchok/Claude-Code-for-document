# Source Notes: RF + AppiumLibrary Mobile Testing

## Appium Core

SOURCE: https://appium.io/docs/en/2.0/intro/
VERSION: 2.0+
CONCEPT: What is Appium
QUOTE: "Appium is an open-source project and ecosystem of related software, designed to facilitate UI automation of many app platforms."

SOURCE: https://appium.io/docs/en/2.0/intro/
VERSION: 2.0+
CONCEPT: Driver Architecture
QUOTE: "A driver is kind of like a pluggable module for Appium that gives Appium the power to automate a particular platform."

SOURCE: https://appium.io/docs/en/2.0/intro/
VERSION: 2.0+
CONCEPT: W3C WebDriver Protocol
QUOTE: "Appium adopted the WebDriver spec as Appium's API in the spirit of joining forces and keeping standards, standard."

## AppiumLibrary for Robot Framework

SOURCE: https://github.com/serhatbolsu/robotframework-appiumlibrary
VERSION: 3.0.0 (July 29, 2025)
CONCEPT: Version + Requirements
QUOTE: "AppiumLibrary v3.0.0 supports Python 3.9 through 3.13, Appium Python Client version 5.1.1 or greater, and Selenium version 4.26.0 or greater."

SOURCE: https://github.com/serhatbolsu/robotframework-appiumlibrary
VERSION: 3.0.0
CONCEPT: Installation
QUOTE: "pip install --upgrade robotframework-appiumlibrary"

SOURCE: https://github.com/serhatbolsu/robotframework-appiumlibrary
VERSION: 3.0.0
CONCEPT: Import in .robot
QUOTE: "*** Settings *** Library    AppiumLibrary"

SOURCE: https://serhatbolsu.github.io/robotframework-appiumlibrary/AppiumLibrary.html
VERSION: 3.0.0
CONCEPT: Open Application keyword
QUOTE: "Opens a new application to the given Appium server"

SOURCE: https://serhatbolsu.github.io/robotframework-appiumlibrary/AppiumLibrary.html
VERSION: 3.0.0
CONCEPT: Close Application keyword
QUOTE: "Closes the current application and the webdriver session"

SOURCE: https://serhatbolsu.github.io/robotframework-appiumlibrary/AppiumLibrary.html
VERSION: 3.0.0
CONCEPT: Click Element keyword
QUOTE: "Clicks the element identified by locator."

SOURCE: https://serhatbolsu.github.io/robotframework-appiumlibrary/AppiumLibrary.html
VERSION: 3.0.0
CONCEPT: Input Text keyword
QUOTE: "Types the given text into the text field identified by locator."

## Appium Capabilities

SOURCE: https://appium.io/docs/en/2.0/guides/caps/
VERSION: 2.0+
CONCEPT: Capabilities Definition
QUOTE: "Capabilities are the set of parameters used to start an Appium session."

SOURCE: https://appium.io/docs/en/2.0/guides/caps/
VERSION: 2.0+
CONCEPT: platformName
QUOTE: "platformName is a required capability specifying the type of platform hosting the app or browser."

SOURCE: https://appium.io/docs/en/2.0/guides/caps/
VERSION: 2.0+
CONCEPT: deviceName
QUOTE: "deviceName is an optional capability representing the name of a particular device to automate, e.g., iPhone 14"

SOURCE: https://appium.io/docs/en/2.0/guides/caps/
VERSION: 2.0+
CONCEPT: automationName (UIAutomator2)
QUOTE: "automationName is required and indicates the name of the Appium driver to use (like XCUITest or UiAutomator2)."

SOURCE: https://appium.io/docs/en/2.0/guides/caps/
VERSION: 2.0+
CONCEPT: appium: prefix convention
QUOTE: "Following W3C WebDriver standards, Appium uses the appium: prefix for vendor-specific capabilities. Examples include: appium:app, appium:deviceName, appium:platformVersion, appium:noReset"

## Locator Strategies

SOURCE: https://www.browserstack.com/guide/locators-in-appium
VERSION: 2026
CONCEPT: Accessibility ID
QUOTE: "Accessibility ID maps to content-desc in Android or accessibility-id in iOS, is nearly as fast as ID, and is recommended when IDs are not available."

SOURCE: https://www.browserstack.com/guide/locators-in-appium
VERSION: 2026
CONCEPT: Resource-ID (Android)
QUOTE: "The ID Locator uses the element's resource-id in Android or the name property in iOS, and is fast, unique, and preferred wherever available."

SOURCE: https://www.browserstack.com/guide/locators-in-appium
VERSION: 2026
CONCEPT: XPath on Mobile — avoid
QUOTE: "XPath scans the whole XML source tree of the application screen, is the only locator in Appium that is not recommended by the Appium team, and has performance issues and is the slowest performing locator strategy."

## Appium Inspector

SOURCE: https://github.com/appium/appium-inspector/releases
VERSION: 2026.1.3
CONCEPT: What is Appium Inspector
QUOTE: "Appium Inspector is a tool for inspecting and interacting with mobile applications during testing. It's available as either a plugin format or desktop app."

## Appium Server Setup (Appium 2.x)

SOURCE: https://appium.io/docs/en/2.0/quickstart/install/
VERSION: 2.0+
CONCEPT: Install Appium globally
QUOTE: "To set up Appium system-wide, run: npm i --location=global appium"

SOURCE: https://appium.io/docs/en/2.0/quickstart/install/
VERSION: 2.0+
CONCEPT: Drivers not included — must install separately
QUOTE: "Simply installing Appium 2.0 will install the Appium server only, but no drivers. To install drivers, you must instead use the new Appium extension CLI."

SOURCE: https://appium.io/docs/en/2.0/quickstart/uiauto2-driver/
VERSION: 2.0+
CONCEPT: Install UIAutomator2 driver
QUOTE: "Execute this command: appium driver install uiautomator2"

SOURCE: https://appium.io/docs/en/2.0/quickstart/uiauto2-driver/
VERSION: 2.0+
CONCEPT: Prerequisites — env vars
QUOTE: "Set ANDROID_HOME (or ANDROID_SDK_ROOT) pointing to your SDK installation directory. Set JAVA_HOME pointing to your JDK home directory. The JDK—not the JRE—is required."
