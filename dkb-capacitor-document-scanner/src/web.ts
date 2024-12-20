import { WebPlugin } from '@capacitor/core';

import type { DocumentScannerPlugin } from './definitions';

export class DocumentScannerWeb extends WebPlugin implements DocumentScannerPlugin {
  constructor() {
    super({
      name: 'DocumentScanner',
      platforms: ['web'],
    });
  }

  async startScan(): Promise<{
    pdfUri?: string;
    pdfPageCount?: number;
    imageUri_0?: string;
    imageUri_1?: string;
  }> {
    console.warn('DocumentScanner is not available on web.');
    return {};
  }
}
