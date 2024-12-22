import { WebPlugin } from '@capacitor/core';

import type { CapacitorMlkitDocumentScannerPlugin } from './definitions';

export class CapacitorMlkitDocumentScannerWeb extends WebPlugin implements CapacitorMlkitDocumentScannerPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
