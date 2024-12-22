export interface CapacitorMlkitDocumentScannerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
