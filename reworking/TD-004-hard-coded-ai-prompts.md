---
type: technical-debt
id: TD-004
status: open
category: Architecture
impact: 3
interest: medium
effort: M

modules:
  - ai

areas:
  - prompt-management
  - ai-config

causes:
  - config-embedded
  - manual-sync

risks:
  - quality
  - maintainability

related:
  - SpringAIConfig
  - AI Prompt Templates
---

## Summary
System prompts are hard-coded inside Java configuration, which makes prompt iteration slow and increases the chance of drift from docs and templates.

## Evidence
- `src/main/java/dev/prospectos/ai/config/SpringAIConfig.java` embeds long prompt strings in `defaultSystem(...)`.
- `docs/01-system-prompts.md` describes prompt content and update steps that can diverge from code.

## Impact
- Prompt updates require code changes and deployments.
- Multiple sources of truth can lead to inconsistencies in AI behavior.

## Direction
- Externalize prompt text into resource files or configuration properties.
- Load prompts by profile or provider so updates can be made without code edits.
- Keep prompt docs linked to the canonical source.

## Links
- [[AI Module]]
- [[Prompt Library]]
