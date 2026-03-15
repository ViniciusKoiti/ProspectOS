import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs';
import { join, relative, resolve } from 'node:path';
import { gzipSync } from 'node:zlib';

const DEFAULT_MAX_GZIP_KB = 500;
const MAX_GZIP_KB = Number(process.env.BUNDLE_GZIP_MAX_KB ?? DEFAULT_MAX_GZIP_KB);
const MAX_GZIP_BYTES = MAX_GZIP_KB * 1024;
const DIST_DIRECTORY = resolve(process.cwd(), 'dist');
const ASSETS_DIRECTORY = join(DIST_DIRECTORY, 'assets');
const BUNDLE_EXTENSIONS = new Set(['.js', '.css']);

function listFilesRecursive(directoryPath) {
    return readdirSync(directoryPath, { withFileTypes: true }).flatMap((entry) => {
        const fullPath = join(directoryPath, entry.name);

        if (entry.isDirectory()) {
            return listFilesRecursive(fullPath);
        }

        return [fullPath];
    });
}

if (!Number.isFinite(MAX_GZIP_KB) || MAX_GZIP_KB <= 0) {
    console.error(`[bundle-check] Invalid BUNDLE_GZIP_MAX_KB value: "${process.env.BUNDLE_GZIP_MAX_KB}".`);
    process.exit(1);
}

if (!existsSync(ASSETS_DIRECTORY)) {
    console.error(`[bundle-check] Missing assets directory: ${ASSETS_DIRECTORY}`);
    console.error('[bundle-check] Run "pnpm build" before this check.');
    process.exit(1);
}

const files = listFilesRecursive(ASSETS_DIRECTORY).filter((filePath) => {
    for (const extension of BUNDLE_EXTENSIONS) {
        if (filePath.endsWith(extension)) {
            return true;
        }
    }

    return false;
});

if (files.length === 0) {
    console.error('[bundle-check] No JS/CSS bundle files found in dist/assets.');
    process.exit(1);
}

let totalRawBytes = 0;
let totalGzipBytes = 0;

for (const filePath of files) {
    const rawBytes = statSync(filePath).size;
    const gzipBytes = gzipSync(readFileSync(filePath)).length;

    totalRawBytes += rawBytes;
    totalGzipBytes += gzipBytes;

    const fileLabel = relative(DIST_DIRECTORY, filePath);
    console.info(`[bundle-check] ${fileLabel} raw=${rawBytes}B gzip=${gzipBytes}B`);
}

console.info(
    `[bundle-check] Total raw=${totalRawBytes}B gzip=${totalGzipBytes}B limit=${MAX_GZIP_BYTES}B (${MAX_GZIP_KB}KB).`
);

if (totalGzipBytes > MAX_GZIP_BYTES) {
    const exceededBy = totalGzipBytes - MAX_GZIP_BYTES;
    console.error(`[bundle-check] FAILED: bundle gzip size exceeded by ${exceededBy}B.`);
    process.exit(1);
}

console.info('[bundle-check] PASSED: bundle gzip size is within the configured limit.');
