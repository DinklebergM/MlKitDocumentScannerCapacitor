import { registerPlugin } from '@capacitor/core';

import type { CapacitorMlkitDocumentScannerPlugin } from './definitions';

const CapacitorMlkitDocumentScanner = registerPlugin<CapacitorMlkitDocumentScannerPlugin>('CapacitorMlkitDocumentScanner', {
  web: () => import('./web').then((m) => new m.CapacitorMlkitDocumentScannerWeb()),
});

export * from './definitions';
export { CapacitorMlkitDocumentScanner };
