export interface StartScanOptions {
  pageLimit?: number;
  galleryImportAllowed?: boolean;
  scannerMode?: 'SCANNER_MODE_BASE' | 'SCANNER_MODE_BASE_WITH_FILTER' | 'SCANNER_MODE_FULL';
  resultFormats?: ('JPEG' | 'PDF')[];
}

export interface Page {
  imageUri: string;
}

export interface Pdf {
  pdfUri: string;
  pageCount: number;
}

export interface ScanResult {
  pages: Page[];
  pdf?: Pdf;
}

export interface CapacitorMlkitDocumentScannerPlugin {
  startScan(options: StartScanOptions): Promise<ScanResult>;
}
