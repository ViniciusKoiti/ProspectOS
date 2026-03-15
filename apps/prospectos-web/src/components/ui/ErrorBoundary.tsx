import type { ErrorInfo, ReactNode } from 'react';
import { Component } from 'react';

import Button from './Button';
import Card from './Card';

type ErrorBoundaryProps = {
    children: ReactNode;
};

type ErrorBoundaryState = {
    hasError: boolean;
    message: string;
};

const INITIAL_STATE: ErrorBoundaryState = {
    hasError: false,
    message: '',
};

export default class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
    public state: ErrorBoundaryState = INITIAL_STATE;

    public static getDerivedStateFromError(error: Error): ErrorBoundaryState {
        return {
            hasError: true,
            message: error.message || 'Unexpected rendering error.',
        };
    }

    public componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
        console.error('Unhandled rendering error in React tree', error, errorInfo);
    }

    private handleRetry = () => {
        this.setState(INITIAL_STATE);
    };

    public render(): ReactNode {
        if (this.state.hasError) {
            return (
                <main className="min-h-screen bg-slate-50 px-4 py-10">
                    <div className="mx-auto max-w-xl">
                        <Card className="space-y-4">
                            <div className="space-y-1">
                                <h1 className="text-xl font-semibold text-slate-900">Something went wrong while rendering this page.</h1>
                                <p className="text-sm text-slate-700">{this.state.message}</p>
                            </div>
                            <div className="flex flex-wrap gap-2">
                                <Button onClick={this.handleRetry} variant="secondary">
                                    Try again
                                </Button>
                                <Button onClick={() => window.location.reload()}>Reload page</Button>
                            </div>
                        </Card>
                    </div>
                </main>
            );
        }

        return this.props.children;
    }
}
