# WireMock Source Notes

SOURCE: https://wiremock.org/docs/standalone/java-jar/
VERSION: 3.13.2
CONCEPT: Standalone JAR run command
QUOTE: "java -jar wiremock-standalone-3.13.2.jar"
QUOTE: "The server runs on port 8080 by default"
QUOTE: "WireMock automatically creates two directories: mappings/ and __files/"

SOURCE: https://wiremock.org/docs/stubbing/
VERSION: 3.13.2
CONCEPT: JSON stub structure
QUOTE: "A WireMock stub mapping requires two main sections: request (specifies matching criteria) and response (defines what to return)"
QUOTE: "jsonBody - JSON object (avoids escaping)"
QUOTE: "bodyFileName - references file in __files directory"

SOURCE: https://wiremock.org/docs/request-matching/
VERSION: 3.13.2
CONCEPT: URL matching types
QUOTE: "url for exact matching, urlPattern for regex patterns"
QUOTE: "Path-only matching is generally preferred for handling multiple query parameters regardless of order"
CONCEPT: Query/Header matching
QUOTE: "queryParameters: { search_term: { equalTo: 'WireMock' } }"

SOURCE: https://wiremock.org/docs/simulating-faults/
VERSION: 3.13.2
CONCEPT: Delay + Fault types
QUOTE: "fixedDelayMilliseconds for per-stub delays"
QUOTE: "Fault enum: EMPTY_RESPONSE, MALFORMED_RESPONSE_CHUNK, RANDOM_DATA_THEN_CLOSE, CONNECTION_RESET_BY_PEER"
QUOTE: "A lognormal distribution is a pretty good approximation of long tailed latencies centered on the 50th percentile"

SOURCE: https://wiremock.org/docs/stateful-behaviour/
VERSION: 3.13.2
CONCEPT: Scenarios
QUOTE: "A scenario is essentially a state machine whose states can be arbitrarily assigned"
QUOTE: "scenarioName, requiredScenarioState, newScenarioState are the three key properties"
QUOTE: "Each scenario begins in the Scenario.STARTED state"

SOURCE: https://wiremock.org/docs/response-templating/
VERSION: 3.13.2
CONCEPT: Response Templating
QUOTE: "Response templating is enabled by default in local mode"
QUOTE: "transformers: ['response-template'] to enable per stub"
QUOTE: "{{now}}, {{randomValue length=10 type='NUMERIC'}}, {{jsonPath request.body '$.field'}}"
