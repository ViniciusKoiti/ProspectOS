import Badge from '../ui/Badge';
import Card from '../ui/Card';

type StatCardProps = {
    label: string;
    value: string;
    trend: string;
};

export default function StatCard({ label, trend, value }: StatCardProps) {
    return (
        <Card className="space-y-2 p-5">
            <p className="text-sm text-slate-600">{label}</p>
            <p className="text-3xl font-semibold tracking-tight">{value}</p>
            <Badge variant="neutral">{trend}</Badge>
        </Card>
    );
}
