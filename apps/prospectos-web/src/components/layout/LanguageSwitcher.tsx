import { useTranslation } from 'react-i18next';

const supportedLanguages = ['pt-BR', 'en', 'es'] as const;

export default function LanguageSwitcher() {
    const { i18n, t } = useTranslation();

    const currentLanguage = supportedLanguages.includes(i18n.resolvedLanguage as (typeof supportedLanguages)[number])
        ? (i18n.resolvedLanguage as (typeof supportedLanguages)[number])
        : 'pt-BR';

    return (
        <label className="flex items-center gap-2 text-xs text-slate-600">
            <span>{t('common.language')}</span>
            <select
                className="rounded-md border border-slate-300 bg-white px-2 py-1 text-xs text-slate-700"
                onChange={(event) => void i18n.changeLanguage(event.target.value)}
                value={currentLanguage}
            >
                {supportedLanguages.map((language) => (
                    <option key={language} value={language}>
                        {t(`common.languageNames.${language}`)}
                    </option>
                ))}
            </select>
        </label>
    );
}
