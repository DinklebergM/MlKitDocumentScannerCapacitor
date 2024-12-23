import { registerPlugin } from '@capacitor/core';

import type { CapacitorMlkitDocumentScannerPlugin } from './definitions';

// Registriere das Plugin mit Namen und Typ
const CapacitorMlkitDocumentScanner = registerPlugin<CapacitorMlkitDocumentScannerPlugin>(
  'CapacitorMlkitDocumentScanner',
  {
    web: () => import('./web').then((m) => new m.CapacitorMlkitDocumentScannerWeb()),
  },
);

export * from './definitions'; // Exportiere die TypeScript-Definitionen
export { CapacitorMlkitDocumentScanner }; // Exportiere das registrierte Plugin
