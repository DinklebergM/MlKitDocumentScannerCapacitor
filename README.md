# capacitor-mlkit-document-scanner

A Capacitor plugin for ML Kit Document Scanner

## Install

```bash
npm install capacitor-mlkit-document-scanner
npx cap sync
```

## API

<docgen-index>

* [`startScan(...)`](#startscan)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### startScan(...)

```typescript
startScan(options: StartScanOptions) => Promise<ScanResult>
```

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#startscanoptions">StartScanOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#scanresult">ScanResult</a>&gt;</code>

--------------------


### Interfaces


#### ScanResult

| Prop        | Type                                |
| ----------- | ----------------------------------- |
| **`pages`** | <code>Page[]</code>                 |
| **`pdf`**   | <code><a href="#pdf">Pdf</a></code> |


#### Page

| Prop           | Type                |
| -------------- | ------------------- |
| **`imageUri`** | <code>string</code> |


#### Pdf

| Prop            | Type                |
| --------------- | ------------------- |
| **`pdfUri`**    | <code>string</code> |
| **`pageCount`** | <code>number</code> |


#### StartScanOptions

| Prop                       | Type                                                                                       |
| -------------------------- | ------------------------------------------------------------------------------------------ |
| **`pageLimit`**            | <code>number</code>                                                                        |
| **`galleryImportAllowed`** | <code>boolean</code>                                                                       |
| **`scannerMode`**          | <code>'SCANNER_MODE_BASE' \| 'SCANNER_MODE_BASE_WITH_FILTER' \| 'SCANNER_MODE_FULL'</code> |
| **`resultFormat`**         | <code>'JPEG' \| 'PDF'</code>                                                               |
| **`lowerQuality`**         | <code>number</code>                                                                        |

</docgen-api>
