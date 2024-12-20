export interface DocumentScannerPlugin {
  startScan(): Promise<{ 
    pdfUri?: string; 
    pdfPageCount?: number; 
    imageUris?: string[];
  }>;
}
