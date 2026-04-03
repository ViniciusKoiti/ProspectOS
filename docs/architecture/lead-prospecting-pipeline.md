# Lead Prospecting Pipeline

## Objective
Define the production pipeline for finding, qualifying, enriching, and contacting leads in ProspectOS.

Supporting documents:
- `docs/architecture/lead-prospecting-use-cases.md`
- `docs/diagrams/lead-prospecting-end-to-end-flow.puml`

This document consolidates the target flow described for:
- data prospecting
- qualification and enrichment
- outbound contact

It also identifies what already exists in the current application and what still needs implementation.

## Pipeline Overview
The active target pipeline has 3 fronts:

1. Data prospecting
- Find companies from a natural-language query such as `Clinicas odontologicas em Orlando`
- Return at least: company name, address, phone, and website

2. Qualification and enrichment
- Identify whether the company has a website
- Evaluate site quality and technical health
- Enrich contacts from the company domain

3. Contact
- Send outbound email through a proper delivery platform

## Front 1: Data Prospecting

### Integration 1: Google Places API
Role:
- Official source for local business discovery
- Primary source for stable and structured data

Expected input:
- Natural-language query such as `restaurants in Orlando`

Expected output:
- name
- formatted address
- national phone number
- website URI
- business type

How it fits ProspectOS:
- Adapter behind a `LeadDiscoverySource`
- Invoked by the lead discovery/search orchestration
- Good default for recommendation when metrics do not indicate degradation

Current status in ProspectOS:
- Implemented as a real discovery source
- Main adapter: `GooglePlacesLeadDiscoverySource`
- Configured by `prospectos.leads.google-places.*`
- Enabled by environment variables loaded through dotenv

Operational notes:
- Requires Google Cloud billing
- Requires Places API enabled
- Requires API key restrictions for production

### Integration 2: Apify / Google Maps Scraper
Role:
- Secondary source for richer or more aggressive data extraction
- Fallback when Google Places is limited by quota, pricing, or result quality

Expected input:
- Natural-language query

Expected output:
- full business record from Google Maps scraping
- potentially richer metadata than the official API

How it fits ProspectOS:
- Adapter behind a `LeadDiscoverySource` or dedicated external integration port
- Good fallback candidate in source recommendation

Current status in ProspectOS:
- Not implemented as a production integration
- Should be treated as a planned external adapter

Operational notes:
- Should use async job execution or webhook callback if the actor is long-running
- Needs explicit quota and cost handling

## Front 2: Qualification and Enrichment

### Integration 3: Google PageSpeed Insights API
Role:
- Technical audit of the company website
- Detect poor performance, mobile weakness, and low-quality implementation

Expected input:
- website URL

Expected output:
- performance score
- mobile friendliness indicators
- Lighthouse-style diagnostics

How it fits ProspectOS:
- Adapter behind a `WebsiteAuditProvider`
- Consumed by prospect enrichment or lead scoring
- Useful to classify leads such as:
  - hot: poor website, no mobile quality, weak performance
  - warm: outdated but still functional

Current status in ProspectOS:
- Not implemented as a real integration
- Recommended as the first qualification adapter after discovery stabilization

Operational notes:
- Free enough for early-stage usage
- Best used after discovery, not during raw search

### Integration 4: Hunter.io or Apollo.io
Role:
- Contact enrichment from company domain
- Find business emails associated with the website

Expected input:
- company domain such as `dentist-orlando.com`

Expected output:
- contact emails
- optionally contact names, positions, confidence, and source metadata

How it fits ProspectOS:
- Adapter behind a `ContactEnrichmentProvider`
- Consumed after discovery and website validation

Current status in ProspectOS:
- Not implemented as a production adapter
- There is already architectural room for enrichment, but not this provider yet

Operational notes:
- Hunter is simpler to start with
- Apollo may become more useful at scale, but is usually heavier operationally

## Front 3: Contact

### Integration 5: Cold Email Delivery
Role:
- Deliver outreach through an email provider designed for scale and reputation handling

Options:
- Resend
- SendGrid
- Lemlist
- Instantly

Recommended split:
- Resend or SendGrid for direct programmatic delivery
- Lemlist or Instantly if campaign warming and cadence orchestration are delegated externally

How it fits ProspectOS:
- Adapter behind an `OutreachDeliveryProvider`
- Invoked after contact enrichment and demo generation

Current status in ProspectOS:
- Outreach exists as an application area, but this delivery integration still needs a production adapter

Operational notes:
- For product simplicity, `Resend` is the best first backend integration
- If follow-up cadence must be first-class inside ProspectOS, implement orchestration internally and keep provider simple

## How This Maps To Current ProspectOS Architecture

### Already present
- Lead search and discovery controllers
- Discovery orchestration
- Source recommendation backed by MCP metrics
- Google Places adapter
- MCP metrics/history/routing support

### Still missing for the full pipeline
- Apify adapter
- PageSpeed adapter
- Hunter or Apollo adapter
- real outbound email provider

## Recommended Delivery Order
1. Google Places as primary production discovery source
2. Source recommendation endpoint and UI integration
3. Apify as fallback discovery source
4. PageSpeed for technical qualification
5. Hunter for contact enrichment
6. Resend for outbound delivery

## Deferred Scope
The internal demo site generator is explicitly out of the active plan for now.

Reason:
- the commercial workflow can remain human-assisted in the proposal stage
- it is not a blocker for discovering, qualifying, enriching, and contacting leads
- removing it reduces product and operational scope while the lead pipeline matures

## Recommended Product Rule
Use MCP as the operational layer, not as the frontend transport.

That means:
- frontend calls normal HTTP endpoints
- backend uses MCP metrics/history to recommend the best source
- adapters remain normal production integrations behind ports

## Summary
This pipeline gives ProspectOS a clear progression:
- discover companies
- audit their digital quality
- enrich contacts
- deliver outreach through a safe provider

The most important point is that Google Places is already the first real production-grade prospecting integration in the codebase. The rest of the pipeline should follow the same adapter-oriented approach.
