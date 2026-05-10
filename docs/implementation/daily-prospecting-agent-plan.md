# Daily Prospecting Agent Plan

## Objective
Capture the idea of a daily agent that automatically searches for new leads, enriches them, ranks them, and presents a shortlist of prospects ready for outreach review.

This is a product evolution on top of the current flow:
1. search
2. qualify
3. enrich
4. deliver

The key difference is that the user does not need to start every search manually. The system proactively prepares a daily queue of leads worth reviewing.

## Product Idea
The application runs a scheduled prospecting agent every day.

The agent:
- executes preconfigured search queries
- uses the discovery sources already integrated in the backend
- enriches selected companies
- ranks them by website weakness, contact availability, and ICP fit
- produces a daily review queue for the user

The user then sees:
- leads found today
- the strongest candidates first
- suggested contacts
- suggested next action

## Core User Outcome
Instead of the user manually searching every day, ProspectOS becomes a daily lead feed.

The system should answer:
- what new leads were found today
- which ones are most promising
- which ones have no website
- which ones have a weak website
- which ones already have a reachable contact
- which ones are ready for outreach review

## Daily Agent Flow
The daily prospecting agent should work like this:

1. Load active prospecting profiles
- each profile defines niche, location, and ICP target
- example:
  - `med spas in Miami-Dade County, Florida`
  - `small dental clinics in Miami, Florida`

2. Execute scheduled discovery
- call the normal discovery flow
- primary source: `google-places`
- fallback source: `apify` when configured or recommended

3. Deduplicate candidates
- avoid repeated leads across days and queries
- use stable lead key, website, phone, and normalized name/location where possible

4. Enrich promising leads
- run `/api/prospect/enrich` semantics internally
- apply:
  - internal website audit
  - PageSpeed audit
  - Hunter domain search when website exists

5. Classify leads
- `NO_WEBSITE`
- `WEAK_WEBSITE`
- `HAS_CONTACT`
- `READY_FOR_OUTREACH`
- `REVIEW_LATER`

6. Rank leads
- rank by:
  - website weakness
  - contact availability
  - ICP fit
  - local business relevance
  - freshness

7. Publish a daily queue
- store or expose a list of leads found today
- return enough detail for the user to review quickly

## Human-in-the-Loop Rule
The agent should not send outreach automatically in the first version.

Recommended rule:
- the agent searches, enriches, and ranks
- the agent suggests the best contact and message direction
- the user approves the outreach
- only then the delivery flow is called

This keeps the system commercially useful while reducing the risk of low-quality outbound.

## Suggested Daily Queue Output
Each daily lead item should include:
- company name
- niche/category
- location
- website presence
- website audit status
- PageSpeed score
- best contact found
- source used
- why the lead is interesting
- recommended next action

Example actions:
- `review for outreach`
- `call by phone`
- `needs contact enrichment`
- `good candidate for website redesign pitch`

## Good First Version
The first implementation should stay simple.

Recommended scope:
- scheduled daily search job
- static list of configured queries
- enrichment for the top N candidates
- persisted daily queue
- UI endpoint to list:
  - leads found today
  - ready-for-review leads
  - leads without website

Do not start with:
- fully autonomous email sending
- complex multi-step cadences
- dynamic AI planning of all search profiles

## Suggested Architecture

### New concepts
- `ProspectingProfile`
  - niche
  - location
  - icpId
  - active flag
  - max daily results

- `DailyProspectingJob`
  - scheduled entry point

- `DailyLeadQueueService`
  - persists and retrieves the queue prepared by the job

- `DailyLeadCandidate`
  - normalized representation of a lead prepared for review

### Existing flows to reuse
- lead search/discovery
- recommendation logic for source choice
- prospect enrichment
- outreach delivery

## Candidate API / UI Surface
Possible future endpoints:

- `GET /api/prospecting/daily-leads`
- `GET /api/prospecting/daily-leads/ready`
- `GET /api/prospecting/daily-leads/no-website`
- `POST /api/prospecting/profiles`
- `PUT /api/prospecting/profiles/{id}`

Frontend view:
- "Today’s leads"
- grouped by:
  - no website
  - weak website
  - has contact
  - ready for outreach

## Ranking Heuristics
The first ranking can be rule-based.

Suggested order:
1. no website
2. weak website + has contact
3. weak website + no contact yet
4. average website + has contact
5. everything else

Extra boosts:
- local niche strongly aligned with the selected ICP
- HTTP-only website
- low PageSpeed score
- strong Hunter confidence

## Why This Idea Is Valuable
This changes ProspectOS from a reactive search tool into a proactive prospecting assistant.

The system stops being:
- "search when I remember"

And becomes:
- "show me today’s best leads to contact"

That is a much stronger daily workflow for:
- agencies
- consultants
- SDRs
- founder-led sales teams

## Recommended Delivery Order
1. add `ProspectingProfile`
2. add scheduled daily prospecting job
3. add daily queue persistence and retrieval
4. expose daily-leads API
5. add UI page for "Today’s leads"
6. add message suggestion and approval workflow

## Summary
The daily prospecting agent should:
- search automatically
- enrich automatically
- rank automatically
- present leads for human approval

This is a strong next-step product direction because it builds directly on the discovery, audit, contact enrichment, and delivery flows already implemented in the repository.
