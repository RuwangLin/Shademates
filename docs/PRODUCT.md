# Product case study

## Product statement

ShadeMates is an Android prototype that helps Melbourne residents make more heat-aware walking decisions. It combines local environmental context, route comparison, shade indicators, cool-place discovery, and concise education in one mobile flow.

The product was developed as a Monash University FIT5120 capstone project in Semester 1, 2026.

## The problem

General weather apps usually describe conditions at city scale, while general mapping apps optimise mostly for time and distance. Neither view fully answers the questions a heat-sensitive commuter may have:

- Which nearby areas appear hotter or cooler?
- Is a slightly longer walking option likely to offer more shade?
- How much UV and heat exposure might this trip involve?
- Where can I pause indoors, in a park, or near water?
- What action can I take now?

ShadeMates treats those questions as one decision-support journey rather than several disconnected searches.

## Intended audience

The team initially explored a broad urban-heat audience, then focused the proposition around daily commuters who may be more sensitive to heat or UV exposure. The design also considers older residents and people with conditions that make outdoor heat more difficult to manage.

The audience definition is a product-design lens, not a clinical classification. ShadeMates does not diagnose risk or replace medical advice.

## Value proposition

> ShadeMates turns heat, UV, shade, and place information into practical route and rest choices.

| User need | Product response |
| --- | --- |
| Understand local conditions quickly | Home summary and interactive heat map |
| Compare walking options | Cool Route and regular-route views |
| Consider more than distance | Temperature, UV, heat exposure, and shade coverage |
| Find relief nearby | Filterable cool-place discovery |
| Know what to do next | Personalised quick tips and awareness content |
| Avoid a fragile all-or-nothing experience | Cached values, local samples, and explicit error states |

## Final feature set

### Home

The home experience groups high-value information into four quick views: all, weather, nearby, and map. It surfaces weather/UV context, tips, a recommended place, and an entry point to the heat map without forcing users through several screens.

### Heat Map

The Mapbox experience visualises relative heat across Melbourne areas, supports panning and zooming, and presents a compact heat snapshot. It is designed for orientation rather than scientific measurement at an exact street point.

### Cool Route

Users search for a destination and compare route options. The result combines:

- distance and duration
- current temperature and UV context
- a heat exposure score
- estimated shade coverage
- cool-place markers along or near a journey

### Cool Places

Nearby places can be filtered by category and walking distance. Examples include parks, indoor spaces, water access, and other useful stops. Selecting a place can hand the destination to the route feature.

### Awareness

Short facts and source labels provide accessible education about heat and UV. The intent is to make the content skimmable during a real-world decision, not to build a long-form reference library.

### Onboarding and personalisation

First launch collects an age group and heat-sensitivity preference. Those choices are stored on device and used as request inputs for relevant personalised features.

## Product principles

1. **Action over data density** - show the next useful choice, not every available measurement.
2. **Progressive detail** - offer a quick answer first and deeper map/route detail when requested.
3. **Plain-language risk communication** - pair a value with a readable label or action.
4. **Graceful degradation** - show cached, sample, empty, or error states instead of crashing.
5. **Data minimisation** - avoid persistent server-side user profiles in the prototype design.
6. **Transparent limits** - communicate that environmental values and route scores are estimates.

## Ethics and safety

ShadeMates is health-adjacent, so misleading certainty is a key design risk. The public showcase therefore frames the app as decision support and retains these boundaries:

- no diagnosis or medical recommendation
- no guarantee that a route is safe
- no emergency alerting promise
- no persistent server-side health profile
- no background location collection described as part of the final prototype
- explicit dependence on third-party data quality and freshness

The public repository also removes the live credentials and internal contact data that were present in the private handover material.

## Roadmap

Potential next steps identified during the project include:

- broader geographic coverage beyond the Melbourne CBD focus
- stronger staleness and confidence indicators for environmental data
- production monitoring and alerting
- more complete offline behaviour
- multilingual and accessibility testing with target users
- public-transport and wearable integrations
- a production backend with server-side upstream secrets
- longitudinal validation of route/exposure scoring assumptions

## Success measures for a future pilot

The coursework prototype was not a production pilot. A future sponsor could evaluate it with:

- task completion rate for finding a lower-exposure route
- comprehension of UV, shade, and heat labels
- time to find a nearby cool place
- agreement between displayed freshness and source timestamps
- route-score calibration against observed shade/temperature samples
- accessibility outcomes for older and heat-sensitive participants
- error-free sessions under weak-network conditions
