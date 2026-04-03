# Lead Prospecting Use Cases

## Objective
Describe the current end-to-end flow of ProspectOS from company discovery to outbound email delivery, and make explicit which user outcomes the application supports.

Related documents:
- `docs/architecture/lead-prospecting-pipeline.md`
- `docs/diagrams/lead-prospecting-end-to-end-flow.puml`
- `docs/diagrams/lead-prospecting-use-cases.puml`

## End-to-End Flow
The current target flow of the application is:

1. Discover companies
- Input: natural-language query such as `clinicas odontologicas em Orlando`
- Sources: `google-places` as primary discovery, `apify` as fallback
- Output: company name, address, phone, website, and source metadata

2. Qualify the website
- Input: company website returned by discovery
- Checks:
  - internal heuristic audit
  - Google PageSpeed technical audit
- Output: website quality score, status, findings, and technical score

3. Enrich contacts
- Input: website/domain
- Provider: Hunter domain search
- Output: prioritized company contacts with email, optional name, optional role, and confidence

4. Deliver outreach
- Input: selected contact and outbound message payload
- Provider: Resend
- Output: delivery id and delivery status

## Use Cases

### 1. Find Local Businesses With Weak Digital Presence
User goal:
- search for local businesses with a high probability of needing website improvement

Application flow:
- user searches by region and niche
- system returns businesses with site and contact basics
- user enriches the best candidates

Value:
- reduces time spent manually browsing Google Maps and business directories

### 2. Prioritize Leads By Technical Quality
User goal:
- identify leads whose website quality suggests stronger commercial urgency

Application flow:
- user enriches a discovered prospect
- system returns internal audit and PageSpeed findings
- user prioritizes low-scoring sites

Value:
- creates objective prioritization instead of intuition-only prospecting

### 3. Turn a Discovered Company Into a Reachable Lead
User goal:
- move from a company website to real contact emails

Application flow:
- user enriches a prospect
- system extracts the domain and calls Hunter
- response includes enriched contacts ready for review

Value:
- shortens the step between research and contact

### 4. Human-in-the-Loop Outbound
User goal:
- keep the sales decision and message review with a human, while removing repetitive lookup work

Application flow:
- system discovers and qualifies the lead
- system enriches contacts
- user reviews the best contact and message
- user triggers delivery

Value:
- maintains commercial control while automating the most repetitive work

### 5. High-Volume Local Prospecting
User goal:
- prospect across multiple cities, niches, or verticals without rebuilding the workflow

Application flow:
- repeated discovery queries run across target markets
- recommendation and provider metrics help choose the best source
- enriched contacts and audit scores standardize triage

Value:
- turns ProspectOS into a repeatable prospecting operation instead of a one-off research tool

## User Potentials

### Agencies and Freelancers
Potential:
- find businesses with poor web performance and convert them into redesign, SEO, or performance opportunities

### SDRs and Outbound Teams
Potential:
- move faster from search to validated contact without splitting work across multiple tools

### Consultancies
Potential:
- use the audit output as a commercial argument backed by technical evidence

### Local Market Operators
Potential:
- run repeated market scans by region, city, or niche
- compare sources and focus effort where the signal is stronger

### Founder-Led Sales
Potential:
- operate a semi-automated outbound pipeline with human review at the final message step

## What The Product Already Supports
- business discovery through real production integrations
- source recommendation from observed operational metrics
- technical website qualification
- contact enrichment by domain
- direct outbound email delivery

## What Still Depends On Operational Choice
- whether outreach copy is generated manually or via AI
- whether contact selection is fully manual or partially automated
- whether delivery remains direct through Resend or later moves to cadence tooling

## Product Reading
ProspectOS is evolving into a lead operations platform with this working chain:

1. search
2. qualify
3. enrich
4. deliver

The system already supports the core mechanics needed for a practical human-assisted outbound motion.
