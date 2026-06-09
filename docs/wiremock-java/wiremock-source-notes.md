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
