---
type: technical-debt
id: TD-005
status: open
category: Architecture
impact: 2
interest: low
effort: P

modules:
  - ai
  - core

areas:
  - demo-code
  - app-startup

causes:
  - unused-code
  - mixed-concerns

risks:
  - maintainability
  - production

related:
  - AIUsageExample
  - SimpleAIDemo
---

## Summary
Demo and example components live in main source sets, which adds non-production behavior and extra beans to the runtime context.

## Evidence
- `src/main/java/dev/prospectos/ai/example/AIUsageExample.java` is a `@Component` with no profile guard.
- `src/main/java/dev/prospectos/ai/example/SimpleAIDemo.java` is a `@Component` under `mock` and `demo` profiles but still ships in main.

## Impact
- Production runtime includes demo code and extra wiring.
- Example paths can become stale and misleading as real workflows evolve.

## Direction
- Move demos to `src/test/java` or a dedicated sample module.
- If needed in main, gate all demo components behind explicit profiles only.

## Links
- [[AI Module]]
- [[Developer Experience]]
