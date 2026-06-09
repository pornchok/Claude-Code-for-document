# WireMock Course — Source Notes

บันทึก QUOTE จาก official docs ก่อนเขียนแต่ละ concept

Format:
```
SOURCE: [URL]
VERSION: [version / date]
CONCEPT: [ชื่อ concept]
QUOTE: "[ข้อความตรงจาก docs — copy โดยตรง ไม่ paraphrase]"
```

---

## บทที่ 1 — Why Mock API

SOURCE: https://wiremock.org/docs/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: What WireMock is
QUOTE: "WireMock is an open source API mocking tool that can help you to create stable test and development environments, isolate yourself from flakey 3rd parties and simulate APIs that don't exist yet."

SOURCE: https://wiremock.org/docs/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Download count / popularity
QUOTE: "WireMock is exceptionally popular, receiving over 5 million monthly downloads."

SOURCE: https://wiremock.org/docs/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Use cases
QUOTE: "Support for multiple protocols including WebSockets, webhooks, gRPC, and GraphQL"

---

## บทที่ 2 — Setup

SOURCE: https://wiremock.org/docs/download-and-installation/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Maven artifact (3.x)
QUOTE: "Group ID: org.wiremock | Artifact ID: wiremock | Version: 3.13.2"

SOURCE: https://wiremock.org/docs/download-and-installation/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Standalone artifact
QUOTE: "Group ID: org.wiremock | Artifact ID: wiremock-standalone | Version: 3.13.2"

---

## บทที่ 3 — Basic Stub (GET)

SOURCE: https://wiremock.org/docs/stubbing/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Basic stubFor API
QUOTE: "stubFor(get(urlEqualTo(\"/some/thing\")).willReturn(aResponse().withHeader(\"Content-Type\", \"text/plain\").withBody(\"Hello world!\")))"

SOURCE: https://wiremock.org/docs/stubbing/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Shorthand methods
QUOTE: "Java offers abbreviated syntax for common patterns: get(\"/path\"), post(\"/path\"), put(\"/path\"), delete(\"/path\") | ok(), ok(\"body\"), okJson(\"{...}\"), unauthorized(), status(418)"

---

## บทที่ 4 — Verify

SOURCE: https://wiremock.org/docs/verifying/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Core verify API
QUOTE: "verify(postRequestedFor(urlEqualTo(\"/verify/this\")).withHeader(\"Content-Type\", equalTo(\"text/xml\")))"

SOURCE: https://wiremock.org/docs/verifying/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Request journal
QUOTE: "The system maintains an in-memory log of received requests that can be queried multiple ways"

SOURCE: https://wiremock.org/docs/verifying/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Count matchers
QUOTE: "exactly(5) — precise count match | lessThan(5) — fewer than specified | moreThan(5) — exceeds count | moreThanOrExactly(5) — meets or exceeds minimum"

SOURCE: https://wiremock.org/docs/verifying/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Count-based verify example
QUOTE: "verify(moreThan(5), postRequestedFor(urlEqualTo(\"/many\")));"

---

## บทที่ 5 — Request Matching: POST Body

SOURCE: https://wiremock.org/docs/request-matching/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: equalToJson basic usage
QUOTE: ".withRequestBody(equalToJson(\"{ \\\"total_results\\\": 4 }\"))"

SOURCE: https://wiremock.org/docs/request-matching/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: equalToJson with ignoreArrayOrder and ignoreExtraElements
QUOTE: ".withRequestBody(equalToJson(\"{ \\\"total_results\\\": 4 }\", true, true))"

SOURCE: https://wiremock.org/docs/request-matching/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: ignoreArrayOrder meaning
QUOTE: "First boolean (ignoreArrayOrder): Treats [1,2] and [2,1] as equivalent"

SOURCE: https://wiremock.org/docs/request-matching/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: ignoreExtraElements meaning
QUOTE: "Second boolean (ignoreExtraElements): Permits additional object properties in actual requests beyond expected fields"

---

## บทที่ 6 — Request Matching: Headers & Query Params

SOURCE: https://wiremock.org/docs/request-matching/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: withHeader matching
QUOTE: ".withHeader(\"Content-Type\", equalTo(\"application/json\"))"

SOURCE: https://wiremock.org/docs/request-matching/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: withQueryParam matching
QUOTE: ".withQueryParam(\"search_term\", equalTo(\"WireMock\"))"

SOURCE: https://wiremock.org/docs/request-matching/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: urlEqualTo vs urlPathEqualTo
QUOTE: "urlEqualTo(\"/your/url?and=query\") — Matches complete URL including query string | urlPathEqualTo(\"/your/url\") — Matches path only; ignores query parameters"

---

## บทที่ 7 — Response Templating

SOURCE: https://wiremock.org/docs/response-templating/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Enable templating globally
QUOTE: "WireMockServer wm = new WireMockServer(options().globalTemplating(true));"

SOURCE: https://wiremock.org/docs/response-templating/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Enable per-stub with withTransformers
QUOTE: "wm.stubFor(get(urlPathEqualTo(\"/templated\")).willReturn(aResponse().withBody(\"{{request.path.[0]}}\").withTransformers(\"response-template\")));"

SOURCE: https://wiremock.org/docs/response-templating/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Template helpers — path segments
QUOTE: "{{request.pathSegments.[0]}} - access by index | {{request.path.[0]}} - alternative syntax"

SOURCE: https://wiremock.org/docs/response-templating/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Template helpers — headers and body
QUOTE: "{{request.headers.X-Request-Id}} - first value | {{request.body}} - full body text"

---

## บทที่ 8 — Scenarios (Stateful Behaviour)

SOURCE: https://wiremock.org/docs/stateful-behaviour/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: How scenarios work
QUOTE: "WireMock implements stateful behavior through scenarios, which function as state machines. Each scenario begins in the Scenario.STARTED state and can transition to custom states based on stub configurations."

SOURCE: https://wiremock.org/docs/stateful-behaviour/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Key methods
QUOTE: "inScenario(String name) — Associates a stub with a named scenario | whenScenarioStateIs(String state) — Matches stubs to specific scenario states | willSetStateTo(String newState) — Transitions the scenario to a new state after matching"

SOURCE: https://wiremock.org/docs/stateful-behaviour/
VERSION: WireMock 3.13.2 / fetched 2026-06-10
CONCEPT: Reset scenarios
QUOTE: "Reset all: POST /__admin/scenarios/reset | Reset one: PUT /__admin/scenarios/{name}/state"
