import { EventEmitter } from 'node:events';
import { createRequire } from 'node:module';
import path from 'node:path';
import { pathToFileURL } from 'node:url';

const require = createRequire(import.meta.url);
const childProcess = require('node:child_process');
const originalExec = childProcess.exec.bind(childProcess);

function createNoopChildProcess() {
    const processHandle = new EventEmitter();
    processHandle.pid = 0;
    processHandle.killed = false;
    processHandle.kill = () => true;
    processHandle.stdout = null;
    processHandle.stderr = null;
    return processHandle;
}

childProcess.exec = function patchedExec(command, options, callback) {
    let resolvedOptions = options;
    let resolvedCallback = callback;

    if (typeof resolvedOptions === 'function') {
        resolvedCallback = resolvedOptions;
        resolvedOptions = undefined;
    }

    if (typeof command === 'string' && command.trim().toLowerCase() === 'net use') {
        if (typeof resolvedCallback === 'function') {
            queueMicrotask(() => resolvedCallback(new Error('Skipping blocked net use call in this environment'), ''));
        }
        return createNoopChildProcess();
    }

    return originalExec(command, resolvedOptions, resolvedCallback);
};

const vitePackagePath = require.resolve('vite/package.json');
const viteCliPath = path.join(path.dirname(vitePackagePath), 'bin', 'vite.js');
await import(pathToFileURL(viteCliPath).href);

