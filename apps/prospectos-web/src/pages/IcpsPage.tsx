import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

import IcpFormModal from '../components/features/IcpFormModal';
import Button from '../components/ui/Button';
import type { DataTableColumn } from '../components/ui/DataTable';
import DataTable from '../components/ui/DataTable';
import ErrorState from '../components/ui/ErrorState';
import LoadingState from '../components/ui/LoadingState';
import Modal from '../components/ui/Modal';
import PageHeader from '../components/ui/PageHeader';
import { createIcp, deleteIcp, listIcps, updateIcp } from '../services/icpService';
import type { Icp, IcpUpsert } from '../types/icpContracts';

export default function IcpsPage() {
    const { t } = useTranslation();
    const queryClient = useQueryClient();
    const [formMode, setFormMode] = useState<'create' | 'edit'>('create');
    const [isFormOpen, setIsFormOpen] = useState(false);
    const [activeIcp, setActiveIcp] = useState<Icp | null>(null);
    const [deleteTarget, setDeleteTarget] = useState<Icp | null>(null);
    const [feedback, setFeedback] = useState<string | null>(null);

    const icpsQuery = useQuery({ queryKey: ['icps'], queryFn: listIcps });
    const createMutation = useMutation({ mutationFn: createIcp });
    const updateMutation = useMutation({
        mutationFn: ({ icpId, payload }: { icpId: string; payload: IcpUpsert }) => updateIcp(icpId, payload),
    });
    const deleteMutation = useMutation({ mutationFn: deleteIcp });

    const resetFormMutations = () => {
        createMutation.reset();
        updateMutation.reset();
    };

    const openCreateModal = () => {
        resetFormMutations();
        setFeedback(null);
        setFormMode('create');
        setActiveIcp(null);
        setIsFormOpen(true);
    };

    const openEditModal = (icp: Icp) => {
        resetFormMutations();
        setFeedback(null);
        setFormMode('edit');
        setActiveIcp(icp);
        setIsFormOpen(true);
    };

    const closeFormModal = () => {
        resetFormMutations();
        setIsFormOpen(false);
        setFormMode('create');
        setActiveIcp(null);
    };

    const closeDeleteModal = () => {
        deleteMutation.reset();
        setDeleteTarget(null);
    };

    const refreshIcps = async () => {
        await queryClient.invalidateQueries({ queryKey: ['icps'] });
    };

    const handleSubmit = async (payload: IcpUpsert) => {
        setFeedback(null);

        try {
            if (formMode === 'edit' && activeIcp) {
                await updateMutation.mutateAsync({ icpId: activeIcp.id, payload });
                await refreshIcps();
                setFeedback(t('pages.icps.feedback.updateSuccess'));
            } else {
                await createMutation.mutateAsync(payload);
                await refreshIcps();
                setFeedback(t('pages.icps.feedback.createSuccess'));
            }

            closeFormModal();
        } catch {
            // Mutation state already carries the error for the form.
        }
    };

    const handleDelete = async () => {
        if (!deleteTarget) {
            return;
        }

        setFeedback(null);

        try {
            await deleteMutation.mutateAsync(deleteTarget.id);
            await refreshIcps();
            setFeedback(t('pages.icps.feedback.deleteSuccess'));
            closeDeleteModal();
        } catch {
            // Mutation state already carries the error for the dialog.
        }
    };

    const columns: DataTableColumn<Icp>[] = [
        { key: 'name', header: t('pages.icps.table.name'), render: (row) => row.name },
        { key: 'focus', header: t('pages.icps.table.focus'), render: (row) => row.targetRoles.join(', ') || '-' },
        { key: 'regions', header: t('pages.icps.table.regions'), render: (row) => row.regions.join(', ') || '-' },
        {
            key: 'actions',
            header: t('common.actions'),
            render: (row) => (
                <div className="flex items-center gap-2">
                    <Button variant="ghost" onClick={() => openEditModal(row)}>
                        {t('common.edit')}
                    </Button>
                    <Button variant="secondary" onClick={() => setDeleteTarget(row)}>
                        {t('common.delete')}
                    </Button>
                </div>
            ),
        },
    ];

    if (icpsQuery.isLoading) {
        return <LoadingState />;
    }

    if (icpsQuery.isError) {
        return <ErrorState message={t('pages.icps.errors.load')} onRetry={() => void icpsQuery.refetch()} />;
    }

    const formError = createMutation.isError || updateMutation.isError ? t('pages.icps.errors.save') : null;
    const deleteError = deleteMutation.isError ? t('pages.icps.errors.delete') : null;

    return (
        <>
            <section className="space-y-4">
                <PageHeader
                    title={t('pages.icps.title')}
                    description={t('pages.icps.description')}
                    action={<Button onClick={openCreateModal}>{t('common.newIcp')}</Button>}
                />

                {feedback ? (
                    <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{feedback}</div>
                ) : null}

                <DataTable
                    columns={columns}
                    rows={icpsQuery.data ?? []}
                    rowKey={(row) => String(row.id)}
                    emptyTitle={t('pages.icps.empty.title')}
                    emptyDescription={t('pages.icps.empty.description')}
                />
            </section>

            <IcpFormModal
                open={isFormOpen}
                mode={formMode}
                icp={formMode === 'edit' ? activeIcp : null}
                isSubmitting={createMutation.isPending || updateMutation.isPending}
                submitError={formError}
                onClose={closeFormModal}
                onSubmit={handleSubmit}
            />

            <Modal
                open={deleteTarget !== null}
                title={t('pages.icps.confirmDelete.title')}
                description={t('pages.icps.confirmDelete.description', { name: deleteTarget?.name ?? '' })}
                onClose={closeDeleteModal}
                footer={
                    <div className="flex items-center justify-end gap-3">
                        <Button variant="secondary" onClick={closeDeleteModal} disabled={deleteMutation.isPending}>
                            {t('common.cancel')}
                        </Button>
                        <Button onClick={() => void handleDelete()} loading={deleteMutation.isPending}>
                            {t('common.delete')}
                        </Button>
                    </div>
                }
            >
                {deleteError ? (
                    <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{deleteError}</div>
                ) : null}
            </Modal>
        </>
    );
}


