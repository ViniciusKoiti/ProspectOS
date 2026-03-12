import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { createIcpFormDefaultValues, type IcpFormInput, icpFormSchema, type IcpFormValues, toIcpUpsert } from '../../features/icps/icpForm';
import type { Icp, IcpUpsert } from '../../types/icpContracts';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Modal from '../ui/Modal';
import TextArea from '../ui/TextArea';

type IcpFormModalProps = {
    open: boolean;
    mode: 'create' | 'edit';
    icp?: Icp | null;
    isSubmitting: boolean;
    submitError?: string | null;
    onClose: () => void;
    onSubmit: (payload: IcpUpsert) => Promise<void>;
};

export default function IcpFormModal({ icp, isSubmitting, mode, onClose, onSubmit, open, submitError }: IcpFormModalProps) {
    const { t } = useTranslation();
    const defaultValues = useMemo(() => createIcpFormDefaultValues(icp ?? undefined), [icp]);
    const form = useForm<IcpFormInput, unknown, IcpFormValues>({
        resolver: zodResolver(icpFormSchema),
        defaultValues,
    });

    useEffect(() => {
        form.reset(defaultValues);
    }, [defaultValues, form, open]);

    const handleSubmit = form.handleSubmit(async (values) => {
        await onSubmit(toIcpUpsert(values));
    });

    const title = mode === 'create' ? t('pages.icps.modal.createTitle') : t('pages.icps.modal.editTitle');
    const description = mode === 'create' ? t('pages.icps.modal.createDescription') : t('pages.icps.modal.editDescription');
    const submitLabel = mode === 'create' ? t('common.create') : t('common.save');
    const multiValueHint = t('pages.icps.form.multiValueHint');

    return (
        <Modal
            open={open}
            title={title}
            description={description}
            onClose={onClose}
            size="lg"
            footer={
                <div className="flex items-center justify-end gap-3">
                    <Button variant="secondary" onClick={onClose} disabled={isSubmitting}>
                        {t('common.cancel')}
                    </Button>
                    <Button type="submit" form="icp-form" loading={isSubmitting}>
                        {submitLabel}
                    </Button>
                </div>
            }
        >
            <form className="space-y-4" id="icp-form" onSubmit={handleSubmit}>
                {submitError ? (
                    <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{submitError}</div>
                ) : null}

                <div className="grid gap-4 md:grid-cols-2">
                    <Input
                        id="icp-name"
                        label={t('pages.icps.form.name')}
                        error={form.formState.errors.name?.message}
                        {...form.register('name')}
                    />
                    <Input
                        id="icp-interest-theme"
                        label={t('pages.icps.form.interestTheme')}
                        error={form.formState.errors.interestTheme?.message}
                        {...form.register('interestTheme')}
                    />
                </div>

                <TextArea
                    id="icp-description"
                    label={t('pages.icps.form.description')}
                    error={form.formState.errors.description?.message}
                    {...form.register('description')}
                />

                <div className="grid gap-4 md:grid-cols-2">
                    <TextArea
                        id="icp-industries"
                        label={t('pages.icps.form.industries')}
                        hint={multiValueHint}
                        error={form.formState.errors.industriesText?.message}
                        {...form.register('industriesText')}
                    />
                    <TextArea
                        id="icp-regions"
                        label={t('pages.icps.form.regions')}
                        hint={multiValueHint}
                        error={form.formState.errors.regionsText?.message}
                        {...form.register('regionsText')}
                    />
                </div>

                <div className="grid gap-4 md:grid-cols-2">
                    <TextArea
                        id="icp-target-roles"
                        label={t('pages.icps.form.targetRoles')}
                        hint={multiValueHint}
                        error={form.formState.errors.targetRolesText?.message}
                        {...form.register('targetRolesText')}
                    />
                    <TextArea
                        id="icp-target-technologies"
                        label={t('pages.icps.form.technologies')}
                        hint={multiValueHint}
                        error={form.formState.errors.targetTechnologiesText?.message}
                        {...form.register('targetTechnologiesText')}
                    />
                </div>

                <div className="grid gap-4 md:grid-cols-2">
                    <Input
                        id="icp-min-employees"
                        type="number"
                        min={0}
                        label={t('pages.icps.form.minEmployees')}
                        error={form.formState.errors.minEmployeeCount?.message}
                        {...form.register('minEmployeeCount')}
                    />
                    <Input
                        id="icp-max-employees"
                        type="number"
                        min={0}
                        label={t('pages.icps.form.maxEmployees')}
                        error={form.formState.errors.maxEmployeeCount?.message}
                        {...form.register('maxEmployeeCount')}
                    />
                </div>
            </form>
        </Modal>
    );
}
