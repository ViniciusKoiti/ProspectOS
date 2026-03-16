import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';

import type { LeadResult } from '../../types/leadContracts';
import Badge from '../ui/Badge';
import Card from '../ui/Card';

type SearchMatchInsightsProps = {
    leads: LeadResult[];
    selectedIcpName?: string | null;
};

type ScoreCategory = 'HOT' | 'WARM' | 'COLD' | 'OTHER';

type SourceInsight = {
    name: string;
    total: number;
    averageScore: number;
    hot: number;
    warm: number;
    cold: number;
};

const SCORE_CATEGORY_ORDER: ScoreCategory[] = ['HOT', 'WARM', 'COLD', 'OTHER'];

function parseScoreCategory(rawCategory: string): ScoreCategory {
    const normalized = rawCategory.trim().toUpperCase();

    if (normalized === 'HOT' || normalized === 'WARM' || normalized === 'COLD') {
        return normalized;
    }

    return 'OTHER';
}

function getBadgeVariant(category: ScoreCategory): 'success' | 'warning' | 'neutral' {
    if (category === 'HOT') {
        return 'success';
    }

    if (category === 'WARM') {
        return 'warning';
    }

    return 'neutral';
}

function getDistributionBarColor(category: ScoreCategory): string {
    if (category === 'HOT') {
        return 'bg-emerald-500';
    }

    if (category === 'WARM') {
        return 'bg-amber-500';
    }

    if (category === 'COLD') {
        return 'bg-slate-500';
    }

    return 'bg-indigo-500';
}

export default function SearchMatchInsights({ leads, selectedIcpName }: SearchMatchInsightsProps) {
    const { t } = useTranslation();

    const insights = useMemo(() => {
        const categoryCounts = new Map<ScoreCategory, number>(
            SCORE_CATEGORY_ORDER.map((category) => [category, 0])
        );
        const sources = new Map<string, { total: number; sum: number; hot: number; warm: number; cold: number }>();
        let scoreSum = 0;

        for (const lead of leads) {
            const category = parseScoreCategory(lead.score.category);
            const sourceName = lead.source.sourceName;
            const sourceCurrent = sources.get(sourceName) ?? {
                total: 0,
                sum: 0,
                hot: 0,
                warm: 0,
                cold: 0,
            };

            scoreSum += lead.score.value;
            categoryCounts.set(category, (categoryCounts.get(category) ?? 0) + 1);

            sourceCurrent.total += 1;
            sourceCurrent.sum += lead.score.value;
            if (category === 'HOT') {
                sourceCurrent.hot += 1;
            }
            if (category === 'WARM') {
                sourceCurrent.warm += 1;
            }
            if (category === 'COLD') {
                sourceCurrent.cold += 1;
            }
            sources.set(sourceName, sourceCurrent);
        }

        const sourceRows: SourceInsight[] = Array.from(sources.entries())
            .map(([name, values]) => ({
                name,
                total: values.total,
                averageScore: values.total === 0 ? 0 : values.sum / values.total,
                hot: values.hot,
                warm: values.warm,
                cold: values.cold,
            }))
            .sort((first, second) => second.total - first.total || first.name.localeCompare(second.name));

        return {
            averageScore: leads.length === 0 ? 0 : scoreSum / leads.length,
            categoryCounts,
            sourceRows,
            totalLeads: leads.length,
            totalSources: sources.size,
        };
    }, [leads]);

    if (leads.length === 0) {
        return null;
    }

    const selectedIcpLabel = selectedIcpName && selectedIcpName.trim().length > 0
        ? selectedIcpName.trim()
        : t('pages.search.insights.noIcpSelected', { defaultValue: 'No ICP selected' });

    return (
        <Card className="space-y-4" data-testid="search-match-insights">
            <div className="space-y-1">
                <h3 className="text-base font-semibold text-slate-900">{t('pages.search.insights.title', { defaultValue: 'Match insights' })}</h3>
                <p className="text-sm text-slate-600">
                    {t('pages.search.insights.subtitle', { defaultValue: 'Quick quality view of the current search result set.' })}
                </p>
            </div>

            <div className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2">
                <div className="text-xs font-medium uppercase tracking-wide text-slate-500">{t('pages.search.insights.icpProfile', { defaultValue: 'ICP profile' })}</div>
                <div className="mt-1 text-base font-semibold text-slate-900">{selectedIcpLabel}</div>
            </div>

            <div className="grid grid-cols-1 gap-2 sm:grid-cols-3">
                <div className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2">
                    <div className="text-xs font-medium uppercase tracking-wide text-slate-500">{t('pages.search.insights.totalLeads', { defaultValue: 'Leads' })}</div>
                    <div className="mt-1 text-2xl font-semibold text-slate-900">{insights.totalLeads}</div>
                </div>
                <div className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2">
                    <div className="text-xs font-medium uppercase tracking-wide text-slate-500">{t('pages.search.insights.averageScore', { defaultValue: 'Average score' })}</div>
                    <div className="mt-1 text-2xl font-semibold text-slate-900">{insights.averageScore.toFixed(1)}</div>
                </div>
                <div className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2">
                    <div className="text-xs font-medium uppercase tracking-wide text-slate-500">{t('pages.search.insights.sources', { defaultValue: 'Sources' })}</div>
                    <div className="mt-1 text-2xl font-semibold text-slate-900">{insights.totalSources}</div>
                </div>
            </div>

            <div className="space-y-2">
                <h4 className="text-sm font-semibold text-slate-800">{t('pages.search.insights.distributionTitle', { defaultValue: 'Match distribution' })}</h4>
                <div className="space-y-2">
                    {SCORE_CATEGORY_ORDER.map((category) => {
                        const count = insights.categoryCounts.get(category) ?? 0;
                        const percentage = insights.totalLeads === 0 ? 0 : (count / insights.totalLeads) * 100;

                        return (
                            <div key={category} className="space-y-1">
                                <div className="flex items-center justify-between">
                                    <Badge variant={getBadgeVariant(category)}>{category}</Badge>
                                    <span className="text-xs font-medium text-slate-600">{count} ({percentage.toFixed(0)}%)</span>
                                </div>
                                <div className="h-2 overflow-hidden rounded bg-slate-200">
                                    <div className={`h-full rounded ${getDistributionBarColor(category)}`} style={{ width: `${percentage}%` }} />
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>

            <div className="space-y-2">
                <h4 className="text-sm font-semibold text-slate-800">{t('pages.search.insights.bySource', { defaultValue: 'Breakdown by source' })}</h4>
                <div className="overflow-hidden rounded-lg border border-slate-200">
                    <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-slate-200">
                            <thead className="bg-slate-50">
                                <tr>
                                    <th className="px-3 py-2 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">
                                        {t('pages.search.insights.sourceName', { defaultValue: 'Source' })}
                                    </th>
                                    <th className="px-3 py-2 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">
                                        {t('pages.search.insights.sourceCount', { defaultValue: 'Leads' })}
                                    </th>
                                    <th className="px-3 py-2 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">
                                        {t('pages.search.insights.sourceAverage', { defaultValue: 'Avg score' })}
                                    </th>
                                    <th className="px-3 py-2 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">HOT/WARM/COLD</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100 bg-white">
                                {insights.sourceRows.map((row) => (
                                    <tr key={row.name}>
                                        <td className="px-3 py-2 text-sm font-medium text-slate-800">{row.name}</td>
                                        <td className="px-3 py-2 text-sm text-slate-700">{row.total}</td>
                                        <td className="px-3 py-2 text-sm text-slate-700">{row.averageScore.toFixed(1)}</td>
                                        <td className="px-3 py-2 text-sm text-slate-700">{row.hot}/{row.warm}/{row.cold}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </Card>
    );
}
