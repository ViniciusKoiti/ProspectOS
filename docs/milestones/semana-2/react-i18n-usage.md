# Frontend i18n usage

Use `common` for labels reused across the whole app, like buttons, language names and generic actions.

Use `nav` only for sidebar and routing labels.

Use `pages` for page-specific copy. Each page should keep its own title, description, table headers and empty-state texts inside its own section.

Use `ui` for generic component text that can appear in many screens, such as loading, error and table fallback messages.

Rule of thumb:
- if the text is reused in multiple places, it should not live under `pages`
- if the text belongs to one screen only, it should not live under `common`
- API data should not be translated in the frontend unless it is a frontend-defined label or enum mapping
